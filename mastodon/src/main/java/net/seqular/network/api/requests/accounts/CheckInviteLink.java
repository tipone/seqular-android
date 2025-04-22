package net.seqular.network.api.requests.accounts;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.api.RequiredField;
import net.seqular.network.model.BaseModel;

public class CheckInviteLink extends MastodonAPIRequest<CheckInviteLink.Response>{
	public CheckInviteLink(String path){
		super(HttpMethod.GET, path, Response.class);
		addHeader("Accept", "application/json");
	}

	@Override
	protected String getPathPrefix(){
		return "";
	}

	public static class Response extends BaseModel{
		@RequiredField
		public String inviteCode;
	}
}
