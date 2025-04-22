package net.seqular.network.api.requests.lists;

import net.seqular.network.api.MastodonAPIRequest;

public class EditListName extends MastodonAPIRequest<Object> {
    public EditListName(String newListName, String listId){
        super(HttpMethod.PUT, "/lists/"+listId, Object.class);
        Request req = new Request();
        req.title = newListName;
        setRequestBody(req);
    }

    public static class Request{
        public String title;
    }
}
