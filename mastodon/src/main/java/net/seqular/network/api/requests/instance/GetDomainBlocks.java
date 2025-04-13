package net.seqular.network.api.requests.instance;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.DomainBlock;

import java.util.List;

public class GetDomainBlocks extends MastodonAPIRequest<List<DomainBlock>>{
	public GetDomainBlocks(){
		super(HttpMethod.GET, "/instance/domain_blocks", new TypeToken<>(){});
	}

}
