package net.seqular.network.api.requests.tags;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Hashtag;

public class GetTag extends MastodonAPIRequest<Hashtag>{
	public GetTag(String tag){
		super(HttpMethod.GET, "/tags/"+tag, Hashtag.class);
	}
}
