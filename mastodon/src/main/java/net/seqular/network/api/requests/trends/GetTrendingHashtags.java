package net.seqular.network.api.requests.trends;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Hashtag;

import java.util.List;

public class GetTrendingHashtags extends MastodonAPIRequest<List<Hashtag>>{
	public GetTrendingHashtags(int limit){
		super(HttpMethod.GET, "/trends", new TypeToken<>(){});
		addQueryParameter("limit", limit+"");
	}
}
