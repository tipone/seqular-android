package org.joinmastodon.android.api.requests.statuses;

import org.joinmastodon.android.api.AllFieldsAreRequired;
import org.joinmastodon.android.api.MastodonAPIRequest;
import org.joinmastodon.android.api.RequiredField;
import org.joinmastodon.android.model.BaseModel;
import org.joinmastodon.android.model.ContentType;

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
