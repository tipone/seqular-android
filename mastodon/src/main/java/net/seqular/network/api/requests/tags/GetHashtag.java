package net.seqular.network.api.requests.tags;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Hashtag;

public class GetHashtag extends MastodonAPIRequest<Hashtag> {
    public GetHashtag(String name){
        super(HttpMethod.GET, "/tags/"+name, Hashtag.class);
    }
}

