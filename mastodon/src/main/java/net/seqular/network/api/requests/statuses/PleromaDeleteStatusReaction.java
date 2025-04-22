package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class PleromaDeleteStatusReaction extends MastodonAPIRequest<Status> {
    public PleromaDeleteStatusReaction(String id, String emoji) {
        super(HttpMethod.DELETE, "/pleroma/statuses/" + id + "/reactions/" + emoji, Status.class);
    }
}
