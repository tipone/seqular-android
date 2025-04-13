package net.seqular.network.api.requests.accounts;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Relationship;

public class SetAccountFollowed extends MastodonAPIRequest<Relationship>{
	public SetAccountFollowed(String id, boolean followed, boolean showReblogs){
		this(id, followed, showReblogs, false);
	}

	public SetAccountFollowed(String id, boolean followed, boolean showReblogs, boolean notify){
		super(HttpMethod.POST, "/accounts/"+id+"/"+(followed ? "follow" : "unfollow"), Relationship.class);
		if(followed)
			setRequestBody(new Request(showReblogs, notify));
		else
			setRequestBody(new Object());
	}

	private static class Request{
		public Boolean reblogs, notify;

		public Request(Boolean reblogs, Boolean notify){
			this.reblogs=reblogs;
			this.notify=notify;
		}
	}
}
