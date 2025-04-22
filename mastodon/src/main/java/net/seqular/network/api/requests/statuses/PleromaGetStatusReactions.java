package net.seqular.network.api.requests.statuses;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.EmojiReaction;

import java.util.List;

public class PleromaGetStatusReactions extends MastodonAPIRequest<List<EmojiReaction>> {
    public PleromaGetStatusReactions(String id, String emoji) {
        super(HttpMethod.GET, "/pleroma/statuses/" + id + "/reactions/" + (emoji != null ? emoji : ""), new TypeToken<>(){});
    }
}
