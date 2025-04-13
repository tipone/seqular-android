package net.seqular.network.api.requests.timelines;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

import java.util.List;

public class GetPublicTimeline extends MastodonAPIRequest<List<Status>>{
	public GetPublicTimeline(boolean local, boolean remote, String maxID, String minID, int limit, String sinceID, String replyVisibility){
		super(HttpMethod.GET, "/timelines/public", new TypeToken<>(){});
		if(local)
			addQueryParameter("local", "true");
		if(remote)
			addQueryParameter("remote", "true");
		if(!TextUtils.isEmpty(maxID))
			addQueryParameter("max_id", maxID);
		if(!TextUtils.isEmpty(minID))
			addQueryParameter("min_id", minID);
		if(!TextUtils.isEmpty(sinceID))
			addQueryParameter("since_id", sinceID);
		if(limit>0)
			addQueryParameter("limit", limit+"");
		if(replyVisibility != null)
			addQueryParameter("reply_visibility", replyVisibility);
	}
}
