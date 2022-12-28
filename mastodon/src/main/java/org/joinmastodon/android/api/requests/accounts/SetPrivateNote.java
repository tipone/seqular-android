package org.joinmastodon.android.api.requests.accounts;

import org.joinmastodon.android.api.MastodonAPIRequest;
import org.joinmastodon.android.model.PrivateNote;

public class SetPrivateNote extends MastodonAPIRequest<PrivateNote>{
    public SetPrivateNote(String id, String note){
        super(MastodonAPIRequest.HttpMethod.POST, "/accounts/"+id+"/note", PrivateNote.class);
        Request req = new Request(note);
        setRequestBody(req);
    }

    public static class Request{
        public String comment;

        public Request(String note){
            this.comment = note;
        }
    }
}
