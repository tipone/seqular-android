package net.seqular.network.api.requests.lists;

import net.seqular.network.api.MastodonAPIRequest;

public class RemoveList extends MastodonAPIRequest<Object> {
    public RemoveList(String listId){
        super(HttpMethod.DELETE, "/lists/"+listId, Object.class);
    }
}
