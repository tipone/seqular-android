package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.api.RequiredField;
import net.seqular.network.model.BaseModel;
import net.seqular.network.model.ContentType;

public class GetStatusSourceText extends MastodonAPIRequest<GetStatusSourceText.Response>{
	public GetStatusSourceText(String id){
		super(HttpMethod.GET, "/statuses/"+id+"/source", Response.class);
	}

	public static class Response extends BaseModel{
		@RequiredField
		public String id;
		@RequiredField
		public String text;
		@RequiredField
		public String spoilerText;
		public ContentType contentType;
	}
}
