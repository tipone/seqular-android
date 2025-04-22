package net.seqular.network.api.requests.oauth;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.model.Application;

public class CreateOAuthApp extends MastodonAPIRequest<Application>{
	public CreateOAuthApp(){
		super(HttpMethod.POST, "/apps", Application.class);
		setRequestBody(new Request());
	}

	private static class Request{
		public String clientName="Seqular";
		public String redirectUris=AccountSessionManager.REDIRECT_URI;
		public String scopes=AccountSessionManager.SCOPE;
		public String website="https://github.com/LucasGGamerM/seqular";
	}
}
