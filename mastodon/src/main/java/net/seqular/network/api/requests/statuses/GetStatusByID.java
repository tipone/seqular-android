package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class GetStatusByID extends MastodonAPIRequest<Status>{
	public GetStatusByID(String id){
		super(HttpMethod.GET, "/statuses/"+id, Status.class);
	}
}
