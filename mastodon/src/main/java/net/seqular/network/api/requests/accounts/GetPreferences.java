package net.seqular.network.api.requests.accounts;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Preferences;

public class GetPreferences extends MastodonAPIRequest<Preferences> {
    public GetPreferences(){
        super(HttpMethod.GET, "/preferences", Preferences.class);
    }
}
