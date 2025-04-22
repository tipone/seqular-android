package net.seqular.network;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import net.seqular.network.api.MastodonAPIController;
import net.seqular.network.api.requests.notifications.GetNotificationByID;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.model.Notification;
import net.seqular.network.model.PaginatedResponse;
import net.seqular.network.model.PushNotification;
import org.unifiedpush.android.connector.FailedReason;
import org.unifiedpush.android.connector.MessagingReceiver;
import org.unifiedpush.android.connector.data.PublicKeySet;
import org.unifiedpush.android.connector.data.PushEndpoint;
import org.unifiedpush.android.connector.data.PushMessage;

import java.util.List;
import java.util.function.Function;

import kotlin.text.Charsets;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;

public class UnifiedPushNotificationReceiver extends MessagingReceiver{
	private static final String TAG="UnifiedPushNotificationReceiver";

	public UnifiedPushNotificationReceiver() {
		super();
	}

	@Override
	public void onNewEndpoint(@NotNull Context context, @NotNull PushEndpoint endpoint, @NotNull String instance) {
		// Called when a new endpoint be used for sending push messages
		Log.d(TAG, "onNewEndpoint: New Endpoint " + endpoint.getUrl() + " for "+ instance);
		AccountSession account = AccountSessionManager.getInstance().tryGetAccount(instance);
		if (account != null) {
			PublicKeySet ks = endpoint.getPubKeySet();
			if (ks != null){
				account.getPushSubscriptionManager().registerAccountForPush(account.pushSubscription, true, endpoint.getUrl(), ks.getPubKey(), ks.getAuth());
			} else {
				// ks should never be null on new endpoint
				account.getPushSubscriptionManager().registerAccountForPush(account.pushSubscription, endpoint.getUrl());
			}
		}
	}

	@Override
	public void onRegistrationFailed(@NotNull Context context, @NotNull FailedReason reason, @NotNull String instance) {
		// called when the registration is not possible, eg. no network
		Log.d(TAG, "onRegistrationFailed: " + instance);
		//re-register for gcm
		AccountSession account = AccountSessionManager.getInstance().tryGetAccount(instance);
		if (account != null)
			account.getPushSubscriptionManager().registerAccountForPush(null);
	}

	@Override
	public void onUnregistered(@NotNull Context context, @NotNull String instance) {
		// called when this application is unregistered from receiving push messages
		Log.d(TAG, "onUnregistered: " + instance);
		//re-register for gcm
		AccountSession account = AccountSessionManager.getInstance().tryGetAccount(instance);
		if (account != null)
			account.getPushSubscriptionManager().registerAccountForPush(null);
	}

	@Override
	public void onMessage(@NotNull Context context, @NotNull PushMessage message, @NotNull String instance) {
		Log.d(TAG, "New message for " + instance);
		// Called when a new message is received. The message contains the full POST body of the push message
		AccountSession account = AccountSessionManager.getInstance().tryGetAccount(instance);

		if (account == null)
		    return;

		if (message.getDecrypted()) {
			// If the mastodon server supports the standard webpush, we can directly use the content
			Log.d(TAG, "Push message correctly decrypted");
			PushNotification pn = MastodonAPIController.gson.fromJson(new String(message.getContent(), Charsets.UTF_8), PushNotification.class);
			new GetNotificationByID(pn.notificationId)
					.setCallback(new Callback<>(){
						@Override
						public void onSuccess(Notification result){
							MastodonAPIController.runInBackground(()->new PushNotificationReceiver().notify(context, pn, instance, result));
						}

						@Override
						public void onError(ErrorResponse error){
							MastodonAPIController.runInBackground(()-> new PushNotificationReceiver().notify(context, pn, instance, null));
						}
					})
					.exec(instance);
		} else {
			// else, we have to sync with the server
			Log.d(TAG, "Server doesn't support standard webpush, fetching one notification");
			fetchOneNotification(context, account, (notif) -> () -> new PushNotificationReceiver().notifyUnifiedPush(context, account, notif));
		}
	}

	private void fetchOneNotification(@NotNull Context context, @NotNull AccountSession account, @NotNull Function<Notification, Runnable> callback) {
		account.getCacheController().getNotifications(null, 1, false, false, true, new Callback<>(){
			@Override
			public void onSuccess(PaginatedResponse<List<Notification>> result){
				result.items
						.stream()
						.findFirst()
						.ifPresent(value->MastodonAPIController.runInBackground(callback.apply(value)));
			}

			@Override
			public void onError(ErrorResponse error){
				//professional error handling
			}
		});
	}
}
