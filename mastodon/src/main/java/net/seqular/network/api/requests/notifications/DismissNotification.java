package net.seqular.network.api.requests.notifications;

import net.seqular.network.api.MastodonAPIRequest;

public class DismissNotification extends MastodonAPIRequest<Object>{
    public DismissNotification(String id){
        super(HttpMethod.POST, "/notifications/" + (id != null ? id + "/dismiss" : "clear"), Object.class);
        setRequestBody(new Object());
    }
}
