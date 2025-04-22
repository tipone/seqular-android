package net.seqular.network.api.requests.lists;

import net.seqular.network.api.ResultlessMastodonAPIRequest;

public class DeleteList extends ResultlessMastodonAPIRequest{
	public DeleteList(String id){
		super(HttpMethod.DELETE, "/lists/"+id);
	}
}
