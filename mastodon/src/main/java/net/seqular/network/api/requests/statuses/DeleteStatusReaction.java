package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class DeleteStatusReaction extends MastodonAPIRequest<Status> {
    public DeleteStatusReaction(String id, String emoji) {
        super(HttpMethod.POST, "/statuses/" + id + "/unreact/" + emoji, Status.class);
		setRequestBody(new Object());
    }
}
