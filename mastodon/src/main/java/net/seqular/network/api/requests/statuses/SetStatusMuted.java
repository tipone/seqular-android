package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class SetStatusMuted extends MastodonAPIRequest<Status>{
	public SetStatusMuted(String id, boolean muted){
		super(HttpMethod.POST, "/statuses/"+id+"/"+(muted ? "mute" : "unmute"), Status.class);
		setRequestBody(new Object());
	}
}
