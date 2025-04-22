package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class SetStatusPinned extends MastodonAPIRequest<Status>{
	public SetStatusPinned(String id, boolean pinned){
		super(HttpMethod.POST, "/statuses/"+id+"/"+(pinned ? "pin" : "unpin"), Status.class);
		setRequestBody(new Object());
	}
}
