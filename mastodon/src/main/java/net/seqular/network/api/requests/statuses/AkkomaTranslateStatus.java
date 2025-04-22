package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.AkkomaTranslation;

public class AkkomaTranslateStatus extends MastodonAPIRequest<AkkomaTranslation>{
	public AkkomaTranslateStatus(String id, String lang){
		super(HttpMethod.GET, "/statuses/"+id+"/translations/"+lang.toLowerCase(), AkkomaTranslation.class);
	}
}
