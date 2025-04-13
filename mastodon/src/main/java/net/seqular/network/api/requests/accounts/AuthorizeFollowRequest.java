package net.seqular.network.api.requests.accounts;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Relationship;

public class AuthorizeFollowRequest extends MastodonAPIRequest<Relationship>{
    public AuthorizeFollowRequest(String id){
        super(HttpMethod.POST, "/follow_requests/"+id+"/authorize", Relationship.class);
        setRequestBody(new Object());
    }
}
