package org.joinmastodon.android.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import org.joinmastodon.android.api.requests.HeaderPaginationRequest;
import org.joinmastodon.android.model.Account;

public class GetAccountBlocks extends HeaderPaginationRequest<Account>{
	public GetAccountBlocks(){
		super(HttpMethod.GET, "/blocks", new TypeToken<>(){});
	}
}
