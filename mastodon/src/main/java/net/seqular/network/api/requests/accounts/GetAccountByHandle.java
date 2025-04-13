package net.seqular.network.api.requests.accounts;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Account;

public class GetAccountByHandle extends MastodonAPIRequest<Account>{
    /**
     * note that this method usually only returns a result if the instance already knows about an
     * account - so it makes sense for looking up local users, search might be preferred otherwise
     */
    public GetAccountByHandle(String acct){
        super(HttpMethod.GET, "/accounts/lookup", Account.class);
        addQueryParameter("acct", acct);
    }
}
