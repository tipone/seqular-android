package net.seqular.network.api.requests.tags;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Hashtag;

public class SetHashtagFollowed extends MastodonAPIRequest<Hashtag>{
    public SetHashtagFollowed(String name, boolean followed){
        super(HttpMethod.POST, "/tags/"+name+"/"+(followed ? "follow" : "unfollow"), Hashtag.class);
        setRequestBody(new Object());
    }
}
