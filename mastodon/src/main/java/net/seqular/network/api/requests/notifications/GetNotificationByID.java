package net.seqular.network.api.requests.notifications;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Notification;

public class GetNotificationByID extends MastodonAPIRequest<Notification>{
	public GetNotificationByID(String id){
		super(HttpMethod.GET, "/notifications/"+id, Notification.class);
	}
}
