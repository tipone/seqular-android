package net.seqular.network.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.requests.HeaderPaginationRequest;
import net.seqular.network.model.Account;

public class GetFollowRequests extends HeaderPaginationRequest<Account>{
	public GetFollowRequests(String maxID, int limit){
		super(HttpMethod.GET, "/follow_requests", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(limit>0)
			addQueryParameter("limit", ""+limit);
	}
}
