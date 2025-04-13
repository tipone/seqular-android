package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class AddStatusReaction extends MastodonAPIRequest<Status> {
	public AddStatusReaction(String id, String emoji) {
		super(HttpMethod.POST, "/statuses/" + id + "/react/" + emoji, Status.class);
		setRequestBody(new Object());
	}
}
