package org.joinmastodon.android.fragments.settings;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.ToNumberPolicy;

import org.joinmastodon.android.BuildConfig;
import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.MastodonApp;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.MastodonAPIController;
import org.joinmastodon.android.api.session.AccountSession;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.fragments.HasAccountID;
import org.joinmastodon.android.model.viewmodel.CheckableListItem;
import org.joinmastodon.android.model.viewmodel.ListItem;
import org.joinmastodon.android.ui.M3AlertDialogBuilder;
import org.joinmastodon.android.ui.Snackbar;
import org.joinmastodon.android.ui.utils.UiUtils;
import org.joinmastodon.android.updater.GithubSelfUpdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.imageloader.ImageCache;
import me.grishka.appkit.utils.MergeRecyclerAdapter;
import me.grishka.appkit.utils.SingleViewRecyclerAdapter;
import me.grishka.appkit.utils.V;

public class SettingsAboutAppFragment extends BaseSettingsFragment<Void> implements HasAccountID{
	private static final String TAG="SettingsAboutAppFragment";
	private static final int IMPORT_RESULT=314;
	private static final int EXPORT_RESULT=271;
	private ListItem<Void> mediaCacheItem, copyCrashLogItem;
	private CheckableListItem<Void> enablePreReleasesItem;
	private AccountSession session;
	private boolean timelineCacheCleared=false;
	private File crashLogFile=new File(MastodonApp.context.getFilesDir(), "crash.log");

	// MOSHIDON
	private ListItem<Void> clearRecentEmojisItem, exportItem, importItem;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.about_app, getString(R.string.mo_app_name)));
		session=AccountSessionManager.get(accountID);

		String lastModified=crashLogFile.exists()
				? DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT).withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(crashLogFile.lastModified()))
				: getString(R.string.sk_settings_crash_log_unavailable);
		List<ListItem<Void>> items=new ArrayList<>(List.of(
				new ListItem<>(R.string.sk_settings_donate, 0, R.drawable.ic_fluent_heart_24_regular, i->UiUtils.launchWebBrowser(getActivity(), getString(R.string.donate_url))),
				new ListItem<>(R.string.mo_settings_contribute, 0, R.drawable.ic_fluent_open_24_regular, i->UiUtils.launchWebBrowser(getActivity(), getString(R.string.repo_url))),
				new ListItem<>(R.string.settings_tos, 0, R.drawable.ic_fluent_open_24_regular, i->UiUtils.launchWebBrowser(getActivity(), "https://"+session.domain+"/terms")),
				new ListItem<>(R.string.settings_privacy_policy, 0, R.drawable.ic_fluent_open_24_regular, i->UiUtils.launchWebBrowser(getActivity(), getString(R.string.privacy_policy_url)), 0, true),
				exportItem=new ListItem<>(R.string.export_settings_title, R.string.export_settings_summary, R.drawable.ic_fluent_arrow_export_24_filled, this::onExportClick),
				importItem=new ListItem<>(R.string.import_settings_title, R.string.import_settings_summary, R.drawable.ic_fluent_arrow_import_24_filled, this::onImportClick, 0, true),
				clearRecentEmojisItem=new ListItem<>(R.string.mo_clear_recent_emoji, 0, this::onClearRecentEmojisClick),
				mediaCacheItem=new ListItem<>(R.string.settings_clear_cache, 0, this::onClearMediaCacheClick),
				new ListItem<>(getString(R.string.sk_settings_clear_timeline_cache), session.domain, this::onClearTimelineCacheClick),
				copyCrashLogItem=new ListItem<>(getString(R.string.sk_settings_copy_crash_log), lastModified, 0, this::onCopyCrashLog)
		));

		if(GithubSelfUpdater.needSelfUpdating() && !BuildConfig.BUILD_TYPE.equals("nightly") ){
			items.add(enablePreReleasesItem=new CheckableListItem<>(R.string.sk_updater_enable_pre_releases, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.enablePreReleases, i->toggleCheckableItem(enablePreReleasesItem)));
		}

		copyCrashLogItem.isEnabled=crashLogFile.exists();
		onDataLoaded(items);
		updateMediaCacheItem();
	}

	@Override
	protected void onHidden(){
		super.onHidden();
		GlobalUserPreferences.enablePreReleases=enablePreReleasesItem!=null && enablePreReleasesItem.checked;
		GlobalUserPreferences.save();
		if(timelineCacheCleared) getActivity().recreate();
	}

	@Override
	protected void doLoadData(int offset, int count){}

	@Override
	protected RecyclerView.Adapter<?> getAdapter(){
		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		adapter.addAdapter(super.getAdapter());

		TextView versionInfo=new TextView(getActivity());
		versionInfo.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, V.dp(32)));
		versionInfo.setTextAppearance(R.style.m3_label_medium);
		versionInfo.setTextColor(UiUtils.getThemeColor(getActivity(), R.attr.colorM3Outline));
		versionInfo.setGravity(Gravity.CENTER);
		versionInfo.setText(getString(R.string.mo_settings_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
		versionInfo.setOnClickListener(v->{
			getActivity().getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("", BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")"));
			if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.S_V2){
				new Snackbar.Builder(getActivity())
						.setText(R.string.app_version_copied)
						.show();
			}
		});
		adapter.addAdapter(new SingleViewRecyclerAdapter(versionInfo));

		return adapter;
	}

	private void onClearMediaCacheClick(ListItem<?> item){
		MastodonAPIController.runInBackground(()->{
			Activity activity=getActivity();
			ImageCache.getInstance(getActivity()).clear();
			activity.runOnUiThread(()->{
				Toast.makeText(activity, R.string.media_cache_cleared, Toast.LENGTH_SHORT).show();
				updateMediaCacheItem();
			});
		});
	}

	private void onClearTimelineCacheClick(ListItem<?> item){
		session.getCacheController().putHomeTimeline(List.of(), true);
		Toast.makeText(getContext(), R.string.sk_timeline_cache_cleared, Toast.LENGTH_SHORT).show();
		timelineCacheCleared=true;
	}

	private void onClearRecentEmojisClick(ListItem<?> item){
		getLocalPrefs().recentCustomEmoji=new ArrayList<>();
		getLocalPrefs().save();
		Toast.makeText(getContext(), R.string.mo_recent_emoji_cleared, Toast.LENGTH_SHORT).show();
	}

	private void onExportClick(ListItem<?> item){
		// The magic will happen on the onActivityResult Method
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.setType("application/json");
		intent.putExtra(Intent.EXTRA_TITLE,"seqular-exported-settings.json");
		startActivityForResult(intent, EXPORT_RESULT);
	}

	private void onImportClick(ListItem<?> item){
		new M3AlertDialogBuilder(getContext())
				.setTitle(R.string.import_settings_confirm)
				.setIcon(R.drawable.ic_fluent_warning_24_regular)
				.setMessage(R.string.import_settings_confirm_body)
				.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
					Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("application/json");
					startActivityForResult(intent, IMPORT_RESULT);
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==IMPORT_RESULT && resultCode==Activity.RESULT_OK){
			Uri uri=data.getData();
			if(uri==null){
				return;
			}
			try{
				InputStream inputStream=getContext().getContentResolver().openInputStream(uri);
				if(inputStream==null)
					return;
				BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder stringBuilder=new StringBuilder();
				String line;
				while((line=reader.readLine())!=null){
					stringBuilder.append(line);
				}
				inputStream.close();
				String jsonString=stringBuilder.toString();

				Gson gson=new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

				//check if json is not null
				if(jsonString.isEmpty()) {
					throw new IOException();
				}

				JsonObject jsonObject=JsonParser.parseString(jsonString).getAsJsonObject();

				//check if json has required attributes
				if(!(jsonObject.has("versionName") && jsonObject.has("versionCode") && jsonObject.has("GlobalUserPreferences"))){
					Toast.makeText(getContext(), getContext().getString(R.string.import_settings_failed), Toast.LENGTH_SHORT).show();
					return;
				}
				String versionName=jsonObject.get("versionName").getAsString();
				int versionCode=jsonObject.get("versionCode").getAsInt();
				Log.i(TAG, "onActivityResult: Reading exported settings ("+versionName+" "+versionCode+")");

				// retrieve GlobalUserPreferences
				Map<String, ?> jsonGlobalPrefs=gson.fromJson(jsonObject.getAsJsonObject("GlobalUserPreferences"), Map.class);
				SharedPreferences.Editor globalPrefsEditor=GlobalUserPreferences.getPrefs().edit();
				for(String key : jsonGlobalPrefs.keySet()){
					Object value=jsonGlobalPrefs.get(key);
					if(value==null)
						continue;
					savePrefValue(globalPrefsEditor, key, value);
				}

				// retrieve LocalPreferences for all logged in accounts
				//TODO: maybe show a dialog for which accounts to import?
				for(AccountSession accountSession : AccountSessionManager.getInstance().getLoggedInAccounts()){
					if(!jsonObject.has(accountSession.self.id))
						continue;
					Map<String, ?> prefs=gson.fromJson(jsonObject.getAsJsonObject(accountSession.self.id), Map.class);

					SharedPreferences.Editor prefEditor=accountSession.getRawLocalPreferences().edit();
					for(String key : prefs.keySet()){
						Object value=prefs.get(key);
						if(value==null)
							continue;
						savePrefValue(prefEditor, key, value);
					}
				}

				// restart app to apply new preferences
				// https://stackoverflow.com/a/46848226
				PackageManager packageManager=getContext().getPackageManager();
				Intent intent=packageManager.getLaunchIntentForPackage(getContext().getPackageName());
				ComponentName componentName=intent.getComponent();
				Intent mainIntent=Intent.makeRestartActivityTask(componentName);
				// Required for API 34 and later
				// Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
				mainIntent.setPackage(getContext().getPackageName());
				getContext().startActivity(mainIntent);
				Runtime.getRuntime().exit(0);
			}catch(IOException e){
				Log.w(TAG, e);
				Toast.makeText(getContext(), getContext().getString(R.string.import_settings_failed), Toast.LENGTH_SHORT).show();
			}
		}

		if(requestCode == EXPORT_RESULT && resultCode==Activity.RESULT_OK) {
			try{
				Gson gson = new Gson();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("versionName", BuildConfig.VERSION_NAME);
				jsonObject.addProperty("versionCode", BuildConfig.VERSION_CODE);

				// GlobalUserPreferences
				//TODO: remove prefs that should not be exported
				JsonElement je = gson.toJsonTree(GlobalUserPreferences.getPrefs().getAll());
				jsonObject.add("GlobalUserPreferences", je);

				// add account local prefs
				for(AccountSession accountSession: AccountSessionManager.getInstance().getLoggedInAccounts()) {
					Map<String, ?> prefs = accountSession.getRawLocalPreferences().getAll();
					//TODO: remove prefs that should not be exported
					JsonElement accountPrefs = gson.toJsonTree(prefs);
					jsonObject.add(accountSession.self.id, accountPrefs);
				}

				File file = new File(getContext().getCacheDir(), "seqular-exported-settings.json");
				FileWriter writer = new FileWriter(file);
				writer.write(jsonObject.toString());
				writer.flush();
				writer.close();

				// Got this from stackoverflow at https://stackoverflow.com/a/67046741
				InputStream is = new FileInputStream(file);
				OutputStream os = getContext().getContentResolver().openOutputStream(data.getData());

				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			}catch(IOException e){
				Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
			}
		}
	}

	private void savePrefValue(SharedPreferences.Editor editor, String key, Object value) {
		if(value.getClass().equals(Boolean.class))
			editor.putBoolean(key, (Boolean) value);
		// gson parses all numbers either long (for int) or double (the rest)
		else if(value.getClass().equals(Long.class))
			editor.putInt(key, ((Long) value).intValue());
		else if(value.getClass().equals(Double.class))
			editor.putFloat(key, ((Double) value).floatValue());
		else
			editor.putString(key, String.valueOf(value));
		//explicitly immediately since the app will restarted soon after
		// and it may not have the time to write the values in the background
		editor.commit();
	}

	private void updateMediaCacheItem(){
		long size=ImageCache.getInstance(getActivity()).getDiskCache().size();
		mediaCacheItem.subtitle=UiUtils.formatFileSize(getActivity(), size, false);
		mediaCacheItem.isEnabled=size>0;
		rebindItem(mediaCacheItem);
	}

	@Override
	public String getAccountID(){
		return accountID;
	}

	private void onCopyCrashLog(ListItem<?> item){
		if(!crashLogFile.exists()) return;
		try(InputStream is=new FileInputStream(crashLogFile)){
			BufferedReader reader=new BufferedReader(new InputStreamReader(is));
			StringBuilder sb=new StringBuilder();
			String line;
			while ((line=reader.readLine())!=null) sb.append(line).append("\n");
			UiUtils.copyText(list, sb.toString());
		} catch(IOException e){
			Log.e(TAG, "Error reading crash log", e);
		}
	}
}
