package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;
import net.seqular.network.model.StatusPrivacy;

public class SetStatusReblogged extends MastodonAPIRequest<Status>{
	public SetStatusReblogged(String id, boolean reblogged, StatusPrivacy visibility){
		super(HttpMethod.POST, "/statuses/"+id+"/"+(reblogged ? "reblog" : "unreblog"), Status.class);
		Request req = new Request();
		req.visibility = visibility;
		setRequestBody(req);
	}

	public static class Request {
		public StatusPrivacy visibility;
	}
}
