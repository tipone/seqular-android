package net.seqular.network.api.requests.filters;

import net.seqular.network.api.ResultlessMastodonAPIRequest;

public class DeleteFilter extends ResultlessMastodonAPIRequest{
	public DeleteFilter(String id){
		super(HttpMethod.DELETE, "/filters/"+id);
	}

	@Override
	protected String getPathPrefix(){
		return "/api/v2";
	}
}
