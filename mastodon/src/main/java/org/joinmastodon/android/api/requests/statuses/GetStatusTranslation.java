package org.joinmastodon.android.api.requests.statuses;

import org.joinmastodon.android.api.AllFieldsAreRequired;
import org.joinmastodon.android.api.MastodonAPIRequest;
import org.joinmastodon.android.model.BaseModel;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.model.StatusTranslation;

import java.util.Locale;

public class GetStatusTranslation extends MastodonAPIRequest<StatusTranslation>{
    public GetStatusTranslation(String id){
        super(HttpMethod.POST, "/statuses/"+id+"/translate", StatusTranslation.class);
        Request r = new Request();
        setRequestBody(r);
    }

    public static class Request{
    }
}
