package net.seqular.network.api.requests.timelines;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

import java.util.List;

public class GetTrendingLinksTimeline extends MastodonAPIRequest<List<Status>>{
	public GetTrendingLinksTimeline(@NonNull String url, String maxID, String minID, int limit){
		super(HttpMethod.GET, "/timelines/link/", new TypeToken<>(){});
		addQueryParameter("url", url);
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(minID!=null)
			addQueryParameter("min_id", minID);
		if(limit>0)
			addQueryParameter("limit", ""+limit);
	}
}
