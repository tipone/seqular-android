package net.seqular.network.api.requests.filters;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Filter;

import java.util.List;

public class GetFilters extends MastodonAPIRequest<List<Filter>>{
	public GetFilters(){
		super(HttpMethod.GET, "/filters", new TypeToken<>(){});
	}

	@Override
	protected String getPathPrefix(){
		return "/api/v2";
	}
}
