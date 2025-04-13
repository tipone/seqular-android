package net.seqular.network.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Hashtag;

import java.util.List;

public class GetAccountFeaturedHashtags extends MastodonAPIRequest<List<Hashtag>>{
	public GetAccountFeaturedHashtags(String id){
		super(HttpMethod.GET, "/accounts/"+id+"/featured_tags", new TypeToken<>(){});
	}
}
