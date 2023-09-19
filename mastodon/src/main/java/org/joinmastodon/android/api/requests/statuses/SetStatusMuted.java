package org.joinmastodon.android.api.requests.statuses;

import org.joinmastodon.android.api.MastodonAPIRequest;
import org.joinmastodon.android.model.Status;

public class SetStatusMuted extends MastodonAPIRequest<Status>{
	public SetStatusMuted(String id, boolean muted){
		super(HttpMethod.POST, "/statuses/"+id+"/"+(muted ? "mute" : "unmute"), Status.class);
		setRequestBody(new Object());
	}
}
