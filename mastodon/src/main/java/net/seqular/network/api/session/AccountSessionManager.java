package net.seqular.network.api.session;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import net.seqular.network.BuildConfig;
import net.seqular.network.E;
import net.seqular.network.ChooseAccountForComposeActivity;
import net.seqular.network.MainActivity;
import net.seqular.network.MastodonApp;
import net.seqular.network.R;
import net.seqular.network.api.MastodonAPIController;
import net.seqular.network.api.PushSubscriptionManager;
import net.seqular.network.api.requests.filters.GetLegacyFilters;
import net.seqular.network.api.requests.instance.GetCustomEmojis;
import net.seqular.network.api.requests.accounts.GetOwnAccount;
import net.seqular.network.api.requests.instance.GetInstance;
import net.seqular.network.api.requests.oauth.CreateOAuthApp;
import net.seqular.network.events.EmojiUpdatedEvent;
import net.seqular.network.model.Account;
import net.seqular.network.model.Application;
import net.seqular.network.model.Emoji;
import net.seqular.network.model.EmojiCategory;
import net.seqular.network.model.LegacyFilter;
import net.seqular.network.model.Instance;
import net.seqular.network.model.Token;
import net.seqular.network.utils.UnifiedPushHelper;
import org.unifiedpush.android.connector.UnifiedPush;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;

public class AccountSessionManager{
	private static final String TAG="AccountSessionManager";
	public static final String SCOPE="read write follow push";
	public static final String REDIRECT_URI = getRedirectURI();

	private static final AccountSessionManager instance=new AccountSessionManager();

	private HashMap<String, AccountSession> sessions=new HashMap<>();
	private HashMap<String, List<EmojiCategory>> customEmojis=new HashMap<>();
	private HashMap<String, Long> instancesLastUpdated=new HashMap<>();
	private HashMap<String, Instance> instances=new HashMap<>();
	private Instance authenticatingInstance;
	private Application authenticatingApp;
	private String lastActiveAccountID;
	private SharedPreferences prefs;
	private boolean loadedInstances;

	public static AccountSessionManager getInstance(){
		return instance;
	}

	public static String getRedirectURI() {
		StringBuilder builder = new StringBuilder();
		builder.append("seqular-android-");
		if (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("nightly")) {
			builder.append(BuildConfig.BUILD_TYPE);
			builder.append('-');
		}
		builder.append("auth://callback");
		return builder.toString();
	}

	private AccountSessionManager(){
		prefs=MastodonApp.context.getSharedPreferences("account_manager", Context.MODE_PRIVATE);
		// This file should not be backed up, otherwise the app may start with accounts already logged in. See res/xml/backup_rules.xml
		File file=new File(MastodonApp.context.getFilesDir(), "accounts.json");
		if(!file.exists())
			return;
		HashSet<String> domains=new HashSet<>();
		try(FileInputStream in=new FileInputStream(file)){
			SessionsStorageWrapper w=MastodonAPIController.gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), SessionsStorageWrapper.class);
			for(AccountSession session:w.accounts){
				domains.add(session.domain.toLowerCase());
				sessions.put(session.getID(), session);
			}
		}catch(Exception x){
			Log.e(TAG, "Error loading accounts", x);
		}
		lastActiveAccountID=prefs.getString("lastActiveAccount", null);
		readInstanceInfo(domains);
		maybeUpdateShortcuts();
	}

	public void addAccount(Instance instance, Token token, Account self, Application app, AccountActivationInfo activationInfo){
		Context context = MastodonApp.context;
		instances.put(instance.uri, instance);
		AccountSession session=new AccountSession(token, self, app, instance.uri, activationInfo==null, activationInfo);
		sessions.put(session.getID(), session);
		lastActiveAccountID=session.getID();
		writeAccountsFile();

		// write initial instance info to file immediately to avoid sessions without instance info
		InstanceInfoStorageWrapper wrapper = new InstanceInfoStorageWrapper();
		wrapper.instance = instance;
		MastodonAPIController.runInBackground(()->writeInstanceInfoFile(wrapper, instance.uri));

		updateMoreInstanceInfo(instance, instance.uri);
		if (UnifiedPushHelper.isUnifiedPushEnabled(context)) {
			UnifiedPush.register(
					context,
					session.getID(),
					null,
					session.app.vapidKey.replaceAll("=","")
			);
		} else if(PushSubscriptionManager.arePushNotificationsAvailable()){
			session.getPushSubscriptionManager().registerAccountForPush(null);
		}
		maybeUpdateShortcuts();
	}

	public synchronized void writeAccountsFile(){
		File tmpFile = new File(MastodonApp.context.getFilesDir(), "accounts.json~");
		File file = new File(MastodonApp.context.getFilesDir(), "accounts.json");
		try{
			try(FileOutputStream out=new FileOutputStream(tmpFile)){
				SessionsStorageWrapper w=new SessionsStorageWrapper();
				w.accounts=new ArrayList<>(sessions.values());
				OutputStreamWriter writer=new OutputStreamWriter(out, StandardCharsets.UTF_8);
				MastodonAPIController.gson.toJson(w, writer);
				writer.flush();
				if (!tmpFile.renameTo(file)) Log.e(TAG, "Error renaming " + tmpFile.getPath() + " to " + file.getPath());
			}
		}catch(IOException x){
			Log.e(TAG, "Error writing accounts file", x);
		}
		prefs.edit().putString("lastActiveAccount", lastActiveAccountID).apply();
	}

	@NonNull
	public List<AccountSession> getLoggedInAccounts(){
		return new ArrayList<>(sessions.values());
	}

	@NonNull
	public AccountSession getAccount(String id){
		AccountSession session=sessions.get(id);
		if(session==null)
			throw new IllegalStateException("Account session "+id+" not found");
		return session;
	}

	public static AccountSession get(String id){
		return getInstance().getAccount(id);
	}

	@Nullable
	public AccountSession tryGetAccount(String id){
		return sessions.get(id);
	}

	public static Optional<AccountSession> getOptional(String id) {
		return Optional.ofNullable(getInstance().tryGetAccount(id));
	}

	@Nullable
	public AccountSession tryGetAccount(Account account) {
		return sessions.get(account.getDomainFromURL() + "_" + account.id);
	}

	@Nullable
	public AccountSession getLastActiveAccount(){
		if(sessions.isEmpty() || lastActiveAccountID==null)
			return null;
		if(!sessions.containsKey(lastActiveAccountID)){
			// TODO figure out why this happens. It should not be possible.
			lastActiveAccountID=getLoggedInAccounts().get(0).getID();
			writeAccountsFile();
		}
		return getAccount(lastActiveAccountID);
	}

	public String getLastActiveAccountID(){
		return lastActiveAccountID;
	}

	public void setLastActiveAccountID(String id){
		if(!sessions.containsKey(id))
			throw new IllegalStateException("Account session "+id+" not found");
		lastActiveAccountID=id;
		prefs.edit().putString("lastActiveAccount", id).apply();
	}

	public void removeAccount(String id){
		AccountSession session=getAccount(id);
		session.getCacheController().closeDatabase();
		session.getCacheController().getListsFile().delete();
		MastodonApp.context.deleteDatabase(id+".db");
		MastodonApp.context.getSharedPreferences(id, 0).edit().clear().commit();
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
			MastodonApp.context.deleteSharedPreferences(id);
		}else{
			String dataDir=MastodonApp.context.getApplicationInfo().dataDir;
			if(dataDir!=null){
				File prefsDir=new File(dataDir, "shared_prefs");
				new File(prefsDir, id+".xml").delete();
			}
		}
		sessions.remove(id);
		if(lastActiveAccountID.equals(id)){
			if(sessions.isEmpty())
				lastActiveAccountID=null;
			else
				lastActiveAccountID=getLoggedInAccounts().get(0).getID();
			prefs.edit().putString("lastActiveAccount", lastActiveAccountID).apply();
		}
		writeAccountsFile();
		String domain=session.domain.toLowerCase();
		if(sessions.isEmpty() || !sessions.values().stream().map(s->s.domain.toLowerCase()).collect(Collectors.toSet()).contains(domain)){
			getInstanceInfoFile(domain).delete();
		}
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
			NotificationManager nm=MastodonApp.context.getSystemService(NotificationManager.class);
			nm.deleteNotificationChannelGroup(id);
		}
		maybeUpdateShortcuts();
	}

	public void authenticate(Activity activity, Instance instance){
		authenticatingInstance=instance;
		new CreateOAuthApp()
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(Application result){
						authenticatingApp=result;
						Uri uri=new Uri.Builder()
								.scheme("https")
								.authority(instance.uri)
								.path("/oauth/authorize")
								.appendQueryParameter("response_type", "code")
								.appendQueryParameter("client_id", result.clientId)
								.appendQueryParameter("redirect_uri", REDIRECT_URI)
								.appendQueryParameter("scope", SCOPE)
								.build();

						new CustomTabsIntent.Builder()
								.setShareState(CustomTabsIntent.SHARE_STATE_OFF)
								.setShowTitle(true)
								.build()
								.launchUrl(activity, uri);
					}

					@Override
					public void onError(ErrorResponse error){
						error.showToast(activity);
					}
				})
				.wrapProgress(activity, R.string.preparing_auth, false)
				.execNoAuth(instance.uri);
	}

	public boolean isSelf(String id, Account other){
		return getAccount(id).self.id.equals(other.id);
	}

	public Instance getAuthenticatingInstance(){
		return authenticatingInstance;
	}

	public Application getAuthenticatingApp(){
		return authenticatingApp;
	}

	public void maybeUpdateLocalInfo(){
		maybeUpdateLocalInfo(null);
	}

	public void maybeUpdateLocalInfo(AccountSession activeSession){
		long now=System.currentTimeMillis();
		HashSet<String> domains=new HashSet<>();
		for(AccountSession session:sessions.values()){
			domains.add(session.domain.toLowerCase());
			if(session == activeSession || now-session.infoLastUpdated>24L*3600_000L){
				session.reloadPreferences(null);
				updateSessionLocalInfo(session);
			}
			if(session == activeSession || (session.getLocalPreferences().serverSideFiltersSupported && now-session.filtersLastUpdated>3600_000L)){
				updateSessionWordFilters(session);
			}
		}
		if(loadedInstances){
			maybeUpdateCustomEmojis(domains, activeSession != null ? activeSession.domain : null);
		}
	}

	private void maybeUpdateCustomEmojis(Set<String> domains, String activeDomain){
		long now=System.currentTimeMillis();
		for(String domain:domains){
			Long lastUpdated=instancesLastUpdated.get(domain);
			if(domain.equals(activeDomain) || lastUpdated==null || now-lastUpdated>24L*3600_000L){
				updateInstanceInfo(domain);
			}
		}
	}

	/*package*/ void updateSessionLocalInfo(AccountSession session){
		new GetOwnAccount()
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(Account result){
						session.self=result;
						session.infoLastUpdated=System.currentTimeMillis();
						session.preferencesFromAccountSource(result);
						writeAccountsFile();
					}

					@Override
					public void onError(ErrorResponse error){

					}
				})
				.exec(session.getID());
	}

	private void updateSessionWordFilters(AccountSession session){
		new GetLegacyFilters()
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(List<LegacyFilter> result){
						session.wordFilters=result;
						session.filtersLastUpdated=System.currentTimeMillis();
						writeAccountsFile();
					}

					@Override
					public void onError(ErrorResponse error){

					}
				})
				.exec(session.getID());
	}

	public void updateInstanceInfo(String domain){
		new GetInstance()
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(Instance instance){
						instances.put(domain, instance);
						updateMoreInstanceInfo(instance, domain);
					}

					@Override
					public void onError(ErrorResponse error){

					}
				})
				.execNoAuth(domain);
	}

	public void updateMoreInstanceInfo(Instance instance, String domain) {
		new GetInstance.V2().setCallback(new Callback<>() {
			@Override
			public void onSuccess(Instance.V2 v2) {
				if (instance != null) instance.v2 = v2;
				updateInstanceEmojis(instance, domain);
			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				updateInstanceEmojis(instance, domain);
			}
		}).execNoAuth(instance.uri);
	}

	private void updateInstanceEmojis(Instance instance, String domain){
		new GetCustomEmojis()
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(List<Emoji> result){
						InstanceInfoStorageWrapper emojis=new InstanceInfoStorageWrapper();
						emojis.lastUpdated=System.currentTimeMillis();
						emojis.emojis=result;
						emojis.instance=instance;
						customEmojis.put(domain, groupCustomEmojis(emojis));
						instancesLastUpdated.put(domain, emojis.lastUpdated);
						MastodonAPIController.runInBackground(()->writeInstanceInfoFile(emojis, domain));
						E.post(new EmojiUpdatedEvent(domain));
					}

					@Override
					public void onError(ErrorResponse error){
						InstanceInfoStorageWrapper wrapper=new InstanceInfoStorageWrapper();
						wrapper.instance = instance;
						MastodonAPIController.runInBackground(()->writeInstanceInfoFile(wrapper, domain));
					}
				})
				.execNoAuth(domain);
	}

	private File getInstanceInfoFile(String domain){
		return new File(MastodonApp.context.getFilesDir(), "instance_"+domain.replace('.', '_')+".json");
	}

	private void writeInstanceInfoFile(InstanceInfoStorageWrapper emojis, String domain){
		File file = getInstanceInfoFile(domain);
		File tmpFile = new File(file.getPath() + "~");
		try(FileOutputStream out=new FileOutputStream(tmpFile)){
			OutputStreamWriter writer=new OutputStreamWriter(out, StandardCharsets.UTF_8);
			MastodonAPIController.gson.toJson(emojis, writer);
			writer.flush();
			if (!tmpFile.renameTo(file)) Log.e(TAG, "Error renaming " + tmpFile.getPath() + " to " + file.getPath());
		}catch(IOException x){
			Log.w(TAG, "Error writing instance info file for "+domain, x);
		}
	}

	private void readInstanceInfo(Set<String> domains){
		for(String domain:domains){
			try(FileInputStream in=new FileInputStream(getInstanceInfoFile(domain))){
				InputStreamReader reader=new InputStreamReader(in, StandardCharsets.UTF_8);
				InstanceInfoStorageWrapper emojis=MastodonAPIController.gson.fromJson(reader, InstanceInfoStorageWrapper.class);
				customEmojis.put(domain, groupCustomEmojis(emojis));
				instances.put(domain, emojis.instance);
				instancesLastUpdated.put(domain, emojis.lastUpdated);
			}catch(Exception x){
				Log.w(TAG, "Error reading instance info file for "+domain, x);
			}
		}
		if(!loadedInstances){
			loadedInstances=true;
			maybeUpdateCustomEmojis(domains, null);
		}
	}

	private List<EmojiCategory> groupCustomEmojis(InstanceInfoStorageWrapper emojis){
		return emojis.emojis.stream()
				.filter(e->e.visibleInPicker)
				.collect(Collectors.groupingBy(e->e.category==null ? "" : e.category))
				.entrySet()
				.stream()
				.map(e->new EmojiCategory(e.getKey(), e.getValue()))
				.sorted(Comparator.comparing(c->c.title))
				.collect(Collectors.toList());
	}

	public List<EmojiCategory> getCustomEmojis(String domain){
		List<EmojiCategory> r=customEmojis.get(domain.toLowerCase());
		return r==null ? Collections.emptyList() : r;
	}

	public Instance getInstanceInfo(String domain){
		return instances.get(domain);
	}

	public void updateAccountInfo(String id, Account account){
		AccountSession session=getAccount(id);
		session.self=account;
		session.infoLastUpdated=System.currentTimeMillis();
		writeAccountsFile();
	}

	private void maybeUpdateShortcuts(){
		if(Build.VERSION.SDK_INT<26)
			return;
		ShortcutManager sm=MastodonApp.context.getSystemService(ShortcutManager.class);

		Intent intent = new Intent(MastodonApp.context, ChooseAccountForComposeActivity.class)
				.setAction(Intent.ACTION_CHOOSER)
				.putExtra("compose", true);

		// This was done so that the old shortcuts get updated to the new implementation.
		if((sm.getDynamicShortcuts().isEmpty() || sm.getDynamicShortcuts().get(0).getIntent() != intent || BuildConfig.DEBUG ) && !sessions.isEmpty()){
			// There are no shortcuts, but there are accounts. Add a compose shortcut.
			ShortcutInfo info=new ShortcutInfo.Builder(MastodonApp.context, "compose")
					.setActivity(ComponentName.createRelative(MastodonApp.context, MainActivity.class.getName()))
					.setShortLabel(MastodonApp.context.getString(R.string.new_post))
					.setIcon(Icon.createWithResource(MastodonApp.context, R.mipmap.ic_shortcut_compose))
					.setIntent(intent)
					.build();
			sm.setDynamicShortcuts(Collections.singletonList(info));
		}else if(sessions.isEmpty()){
			// There are shortcuts, but no accounts. Disable existing shortcuts.
			sm.disableShortcuts(Collections.singletonList("compose"), MastodonApp.context.getString(R.string.err_not_logged_in));
		}else{
			sm.enableShortcuts(Collections.singletonList("compose"));
		}
	}

	private static class SessionsStorageWrapper{
		public List<AccountSession> accounts;
	}

	private static class InstanceInfoStorageWrapper{
		public Instance instance;
		public List<Emoji> emojis;
		public long lastUpdated;
	}
}
