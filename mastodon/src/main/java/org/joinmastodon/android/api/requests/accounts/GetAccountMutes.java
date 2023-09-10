package org.joinmastodon.android.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import org.joinmastodon.android.api.requests.HeaderPaginationRequest;
import org.joinmastodon.android.model.Account;

public class GetAccountMutes extends HeaderPaginationRequest<Account>{
	public GetAccountMutes(){
		super(HttpMethod.GET, "/mutes/", new TypeToken<>(){});
	}
}
