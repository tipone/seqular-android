package net.seqular.network.api.requests.instance;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.ExtendedDescription;

public class GetExtendedDescription extends MastodonAPIRequest<ExtendedDescription>{
	public GetExtendedDescription(){
		super(HttpMethod.GET, "/instance/extended_description", ExtendedDescription.class);
	}

}
