package net.seqular.network.api.requests.announcements;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Announcement;

import java.util.List;

public class GetAnnouncements extends MastodonAPIRequest<List<Announcement>> {
    public GetAnnouncements(boolean withDismissed) {
        super(MastodonAPIRequest.HttpMethod.GET, "/announcements", new TypeToken<>(){});
        addQueryParameter("with_dismissed", withDismissed ? "true" : "false");
    }
}
