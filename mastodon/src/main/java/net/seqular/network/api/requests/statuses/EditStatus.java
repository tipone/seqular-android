package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

public class EditStatus extends MastodonAPIRequest<Status>{
	public EditStatus(CreateStatus.Request req, String id){
		super(HttpMethod.PUT, "/statuses/"+id, Status.class);
		setRequestBody(req);
	}
}
