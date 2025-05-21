package net.seqular.network.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.seqular.network.api.requests.oauth.GetOauthToken;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import org.unifiedpush.android.connector.UnifiedPush;

public class UnifiedPushHelper {

	/**
	 * @param context
	 * @return `true` if UnifiedPush is used
	 */
	public static boolean isUnifiedPushEnabled(@NonNull Context context) {
		return UnifiedPush.getAckDistributor(context) != null;
	}

	/**
	 * If any distributor is installed on the device
	 * @param context
	 * @return `true` if at least one is installed
	 */
	public static boolean hasAnyDistributorInstalled(@NonNull Context context) {
		return !UnifiedPush.getDistributors(context).isEmpty();
	}

	public static void registerAllAccounts(@NonNull Context context) {
		for (AccountSession accountSession : AccountSessionManager.getInstance().getLoggedInAccounts()){
			// Sometimes this is null when the account's server has died (don't ask me how I know this)
			if (accountSession.app.vapidKey == null) {
				// TODO: throw this on a translatable string and tell the user to log out and back in
				Toast.makeText(context, "Error on unified push subscription: no valid vapid key for account " + accountSession.getFullUsername(), Toast.LENGTH_LONG).show();
				break;
			}
			UnifiedPush.register(
					context,
					accountSession.getID(),
					null,
					accountSession.app.vapidKey.replaceAll("=","")
			);
		}
	}

	public static void unregisterAllAccounts(@NonNull Context context) {
		for (AccountSession accountSession : AccountSessionManager.getInstance().getLoggedInAccounts()){
			UnifiedPush.unregister(
				context,
				accountSession.getID()
			);
			// use FCM again
			accountSession.getPushSubscriptionManager().registerAccountForPush(null);
		}
	}
}
