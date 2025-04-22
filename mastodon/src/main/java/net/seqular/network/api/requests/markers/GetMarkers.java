package net.seqular.network.api.requests.markers;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.TimelineMarkers;

public class GetMarkers extends MastodonAPIRequest<TimelineMarkers>{
	public GetMarkers(){
		super(HttpMethod.GET, "/markers", TimelineMarkers.class);
		addQueryParameter("timeline[]", "home");
		addQueryParameter("timeline[]", "notifications");
	}
}
