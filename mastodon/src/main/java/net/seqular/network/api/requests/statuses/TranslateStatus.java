package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Translation;

import java.util.Map;

public class TranslateStatus extends MastodonAPIRequest<Translation>{
	public TranslateStatus(String id, String lang){
		super(HttpMethod.POST, "/statuses/"+id+"/translate", Translation.class);
		setRequestBody(Map.of("lang", lang));
	}
}
