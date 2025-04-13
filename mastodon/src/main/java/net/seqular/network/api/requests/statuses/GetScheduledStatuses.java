package net.seqular.network.api.requests.statuses;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.requests.HeaderPaginationRequest;
import net.seqular.network.model.ScheduledStatus;

public class GetScheduledStatuses extends HeaderPaginationRequest<ScheduledStatus>{
	public GetScheduledStatuses(String maxID, int limit){
		super(HttpMethod.GET, "/scheduled_statuses", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(limit>0)
			addQueryParameter("limit", limit+"");
	}
}
