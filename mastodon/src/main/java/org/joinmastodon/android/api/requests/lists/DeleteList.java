package org.joinmastodon.android.api.requests.lists;

import android.app.ListFragment;

import org.joinmastodon.android.api.MastodonAPIRequest;

public class DeleteList extends MastodonAPIRequest<ListFragment> {
    public DeleteList(String id){
        super(HttpMethod.DELETE, "/lists/"+id, ListFragment.class);
    }
}
