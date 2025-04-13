package net.seqular.network.api.requests.announcements;

import net.seqular.network.api.MastodonAPIRequest;

public class AddAnnouncementReaction extends MastodonAPIRequest<Object> {
	public AddAnnouncementReaction(String id, String emoji) {
		super(HttpMethod.PUT, "/announcements/" + id + "/reactions/" + emoji, Object.class);
		setRequestBody(new Object());
	}
}
