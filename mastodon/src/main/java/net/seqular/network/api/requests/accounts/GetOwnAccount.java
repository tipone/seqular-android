package net.seqular.network.api.requests.accounts;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Account;

public class GetOwnAccount extends MastodonAPIRequest<Account>{
	public GetOwnAccount(){
		super(HttpMethod.GET, "/accounts/verify_credentials", Account.class);
	}
}
