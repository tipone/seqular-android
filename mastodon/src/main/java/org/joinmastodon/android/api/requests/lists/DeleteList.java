package org.joinmastodon.android.api.requests.lists;

import org.joinmastodon.android.api.MastodonAPIRequest;

public class DeleteList extends MastodonAPIRequest<Object> {
    public DeleteList(String listId){
        super(HttpMethod.DELETE, "/lists/"+listId, Object.class);
    }
}
