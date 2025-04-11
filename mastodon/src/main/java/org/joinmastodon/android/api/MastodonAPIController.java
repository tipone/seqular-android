package org.joinmastodon.android.api;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.joinmastodon.android.BuildConfig;
import org.joinmastodon.android.MastodonApp;
import org.joinmastodon.android.api.gson.IsoInstantTypeAdapter;
import org.joinmastodon.android.api.gson.IsoLocalDateTypeAdapter;
import org.joinmastodon.android.api.session.AccountSession;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.grishka.appkit.utils.WorkerThread;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MastodonAPIController{
	private static final String TAG="MastodonAPIController";
	public static final Gson gsonWithoutDeserializer = new GsonBuilder()
			.disableHtmlEscaping()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(Instant.class, new IsoInstantTypeAdapter())
			.registerTypeAdapter(LocalDate.class, new IsoLocalDateTypeAdapter())
			.create();
	public static final Gson gson = gsonWithoutDeserializer.newBuilder()
			.registerTypeAdapter(Status.class, new Status.StatusDeserializer())
			.create();
	private static WorkerThread thread=new WorkerThread("MastodonAPIController");
	private static OkHttpClient httpClient=new OkHttpClient.Builder()
			.connectTimeout(60, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build();

	private AccountSession session;
	private static List<String> badDomains = new ArrayList<>();

	static{
		thread.start();
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(
					MastodonApp.context.getAssets().open("blocks.txt")
			));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank() || line.startsWith("#")) continue;
				String[] parts = line.replaceAll("\"", "").split("[\s,;]");
				if (parts.length == 0) continue;
				String domain = parts[0].toLowerCase().trim();
				if (domain.isBlank()) continue;
				badDomains.add(domain);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MastodonAPIController(@Nullable AccountSession session){
		this.session=session;
	}

	public <T> void submitRequest(final MastodonAPIRequest<T> req){
		final String host = req.getURL().getHost();
		final boolean isBad = host == null || badDomains.stream().anyMatch(h -> h.equalsIgnoreCase(host) || host.toLowerCase().endsWith("." + h));
		thread.postRunnable(()->{
			try{
				if(isBad){
					Log.i(TAG, "submitRequest: refusing to connect to bad domain: " + host);
					throw new IllegalArgumentException("Failed to connect to domain");
				}

				if(req.canceled)
					return;
				Request.Builder builder=new Request.Builder()
						.url(req.getURL().toString())
						.method(req.getMethod(), req.getRequestBody())
						.header("User-Agent", "SeqularAndroid/"+BuildConfig.VERSION_NAME);

				String token=null;
				if(session!=null)
					token=session.token.accessToken;
				else if(req.token!=null)
					token=req.token.accessToken;

				if(token!=null)
					builder.header("Authorization", "Bearer "+token);

				if(req.headers!=null){
					for(Map.Entry<String, String> header:req.headers.entrySet()){
						builder.header(header.getKey(), header.getValue());
					}
				}

				Request hreq=builder.build();
				OkHttpClient client=req.timeout>0
						? httpClient.newBuilder().readTimeout(req.timeout, TimeUnit.MILLISECONDS).build()
						: httpClient;
				Call call=client.newCall(hreq);
				synchronized(req){
					req.okhttpCall=call;
				}

				if(BuildConfig.DEBUG)
					Log.d(TAG, logTag(session)+"Sending request: "+hreq);

				call.enqueue(new Callback(){
					@Override
					public void onFailure(@NonNull Call call, @NonNull IOException e){
						if(req.canceled)
							return;
						if(BuildConfig.DEBUG)
							Log.w(TAG, logTag(session)+""+hreq+" failed", e);
						synchronized(req){
							req.okhttpCall=null;
						}
						req.onError(e.getLocalizedMessage(), 0, e);
					}

					@Override
					public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
						if(req.canceled)
							return;
						if(BuildConfig.DEBUG)
							Log.d(TAG, logTag(session)+hreq+" received response: "+response);
						synchronized(req){
							req.okhttpCall=null;
						}
						try(ResponseBody body=response.body()){
							Reader reader=body.charStream();
							if(response.isSuccessful()){
								T respObj;
								try{
									if(BuildConfig.DEBUG){
										JsonElement respJson=JsonParser.parseReader(reader);
										Log.d(TAG, logTag(session)+"response body: "+respJson);
										if(req.respTypeToken!=null)
											respObj=gson.fromJson(respJson, req.respTypeToken.getType());
										else if(req.respClass!=null)
											respObj=gson.fromJson(respJson, req.respClass);
										else
											respObj=null;
									}else{
										if(req.respTypeToken!=null)
											respObj=gson.fromJson(reader, req.respTypeToken.getType());
										else if(req.respClass!=null)
											respObj=gson.fromJson(reader, req.respClass);
										else
											respObj=null;
									}
								}catch(JsonIOException|JsonSyntaxException x){
									if (req.context != null && response.body().contentType().subtype().equals("html")) {
										UiUtils.launchWebBrowser(req.context, response.request().url().toString());
										req.cancel();
										return;
									}
									if(BuildConfig.DEBUG)
										Log.w(TAG, logTag(session)+response+" error parsing or reading body", x);
									req.onError(x.getLocalizedMessage(), response.code(), x);
									return;
								}

								try{
									req.validateAndPostprocessResponse(respObj, response);
								}catch(IOException x){
									if(BuildConfig.DEBUG)
										Log.w(TAG, logTag(session)+response+" error post-processing or validating response", x);
									req.onError(x.getLocalizedMessage(), response.code(), x);
									return;
								}

								if(BuildConfig.DEBUG)
									Log.d(TAG, logTag(session)+response+" parsed successfully: "+respObj);

								req.onSuccess(respObj);
							}else{
								try{
									JsonObject error=JsonParser.parseReader(reader).getAsJsonObject();
									Log.w(TAG, logTag(session)+response+" received error: "+error);
									if(error.has("details")){
										MastodonDetailedErrorResponse err=new MastodonDetailedErrorResponse(error.get("error").getAsString(), response.code(), null);
										HashMap<String, List<MastodonDetailedErrorResponse.FieldError>> details=new HashMap<>();
										JsonObject errorDetails=error.getAsJsonObject("details");
										for(String key:errorDetails.keySet()){
											ArrayList<MastodonDetailedErrorResponse.FieldError> fieldErrors=new ArrayList<>();
											for(JsonElement el:errorDetails.getAsJsonArray(key)){
												JsonObject eobj=el.getAsJsonObject();
												MastodonDetailedErrorResponse.FieldError fe=new MastodonDetailedErrorResponse.FieldError();
												fe.description=eobj.get("description").getAsString();
												fe.error=eobj.get("error").getAsString();
												fieldErrors.add(fe);
											}
											details.put(key, fieldErrors);
										}
										err.detailedErrors=details;
										req.onError(err);
									}else{
										req.onError(error.get("error").getAsString(), response.code(), null);
									}
								}catch(JsonIOException|JsonSyntaxException x){
									req.onError(response.code()+" "+response.message(), response.code(), x);
								}catch(Exception x){
									req.onError("Error parsing an API error", response.code(), x);
								}
							}
						}catch(Exception x){
							Log.w(TAG, "onResponse: error processing response", x);
							onFailure(call, (IOException) new IOException(x).fillInStackTrace());
						}
					}
				});
			}catch(Exception x){
				if(BuildConfig.DEBUG)
					Log.w(TAG, logTag(session)+"error creating and sending http request", x);
				req.onError(x.getLocalizedMessage(), 0, x);
			}
		}, 0);
	}

	public static void runInBackground(Runnable action){
		thread.postRunnable(action, 0);
	}

	public static OkHttpClient getHttpClient(){
		return httpClient;
	}

	private static String logTag(AccountSession session){
		return "["+(session==null ? "no-auth" : session.getID())+"] ";
	}
}
