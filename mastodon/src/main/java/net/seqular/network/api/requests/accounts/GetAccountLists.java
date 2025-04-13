package net.seqular.network.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.FollowList;

import java.util.List;

public class GetAccountLists extends MastodonAPIRequest<List<FollowList>>{
	public GetAccountLists(String id){
		super(HttpMethod.GET, "/accounts/"+id+"/lists", new TypeToken<>(){});
	}
}
