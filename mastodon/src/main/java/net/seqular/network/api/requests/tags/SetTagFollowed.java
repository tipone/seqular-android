package net.seqular.network.api.requests.tags;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Hashtag;

public class SetTagFollowed extends MastodonAPIRequest<Hashtag>{
	public SetTagFollowed(String tag, boolean followed){
		super(HttpMethod.POST, "/tags/"+tag+(followed ? "/follow" : "/unfollow"), Hashtag.class);
		setRequestBody(new Object());
	}
}
