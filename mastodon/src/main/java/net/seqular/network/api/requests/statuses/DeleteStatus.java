package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class DeleteStatus extends MastodonAPIRequest<Status>{
	public DeleteStatus(String id){
		super(HttpMethod.DELETE, "/statuses/"+id, Status.class);
	}

	public static class Scheduled extends MastodonAPIRequest<Object> {
		public Scheduled(String id) {
			super(HttpMethod.DELETE, "/scheduled_statuses/"+id, Object.class);
		}
	}
}
