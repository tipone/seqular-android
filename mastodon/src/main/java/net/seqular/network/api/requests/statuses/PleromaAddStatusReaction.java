package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class PleromaAddStatusReaction extends MastodonAPIRequest<Status> {
    public PleromaAddStatusReaction(String id, String emoji) {
        super(HttpMethod.PUT, "/pleroma/statuses/" + id + "/reactions/" + emoji, Status.class);
		setRequestBody(new Object());
    }
}
