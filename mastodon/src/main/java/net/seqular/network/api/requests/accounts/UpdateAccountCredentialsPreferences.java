package net.seqular.network.api.requests.accounts;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Account;
import net.seqular.network.model.Preferences;
import net.seqular.network.model.StatusPrivacy;

public class UpdateAccountCredentialsPreferences extends MastodonAPIRequest<Account>{
	public UpdateAccountCredentialsPreferences(Preferences preferences, Boolean locked, Boolean discoverable, Boolean indexable){
		super(HttpMethod.PATCH, "/accounts/update_credentials", Account.class);
		setRequestBody(new Request(locked, discoverable, indexable, new RequestSource(preferences.postingDefaultVisibility, preferences.postingDefaultLanguage)));
	}

	private static class Request{
		public Boolean locked, discoverable, indexable;
		public RequestSource source;

		public Request(Boolean locked, Boolean discoverable, Boolean indexable, RequestSource source){
			this.locked=locked;
			this.discoverable=discoverable;
			this.indexable=indexable;
			this.source=source;
		}
	}

	private static class RequestSource{
		public StatusPrivacy privacy;
		public String language;

		public RequestSource(StatusPrivacy privacy, String language){
			this.privacy=privacy;
			this.language=language;
		}
	}
}
