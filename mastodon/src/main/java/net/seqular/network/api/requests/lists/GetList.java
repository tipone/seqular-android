package net.seqular.network.api.requests.lists;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.FollowList;

public class GetList extends MastodonAPIRequest<FollowList> {
	public GetList(String id) {
		super(HttpMethod.GET, "/lists/" + id, FollowList.class);
	}
}
