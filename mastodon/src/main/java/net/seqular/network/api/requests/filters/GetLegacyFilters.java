package net.seqular.network.api.requests.filters;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.LegacyFilter;

import java.util.List;

public class GetLegacyFilters extends MastodonAPIRequest<List<LegacyFilter>>{
	public GetLegacyFilters(){
		super(HttpMethod.GET, "/filters", new TypeToken<>(){});
	}
}
