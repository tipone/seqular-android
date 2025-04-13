package net.seqular.network.api.requests.announcements;

import net.seqular.network.api.MastodonAPIRequest;

public class DismissAnnouncement extends MastodonAPIRequest<Object>{
	public DismissAnnouncement(String id){
		super(HttpMethod.POST, "/announcements/" + id + "/dismiss", Object.class);
		setRequestBody(new Object());
	}
}
