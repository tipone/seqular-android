package net.seqular.network.api.requests.announcements;

import net.seqular.network.api.MastodonAPIRequest;

public class DeleteAnnouncementReaction extends MastodonAPIRequest<Object> {
	public DeleteAnnouncementReaction(String id, String emoji) {
		super(HttpMethod.DELETE, "/announcements/" + id + "/reactions/" + emoji, Object.class);
	}
}
