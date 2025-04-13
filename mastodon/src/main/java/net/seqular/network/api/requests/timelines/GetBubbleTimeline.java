package net.seqular.network.api.requests.timelines;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Status;

import java.util.List;

public class GetBubbleTimeline extends MastodonAPIRequest<List<Status>> {
    public GetBubbleTimeline(String maxID, int limit, String replyVisibility) {
        super(HttpMethod.GET, "/timelines/bubble", new TypeToken<>(){});
        if(!TextUtils.isEmpty(maxID))
            addQueryParameter("max_id", maxID);
        if(limit>0)
            addQueryParameter("limit", limit+"");
        if(replyVisibility != null)
            addQueryParameter("reply_visibility", replyVisibility);
    }
}
