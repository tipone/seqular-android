package net.seqular.network.api.requests.lists;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.requests.HeaderPaginationRequest;
import net.seqular.network.model.Account;

public class GetListAccounts extends HeaderPaginationRequest<Account>{
	public GetListAccounts(String listID, String maxID, int limit){
		super(HttpMethod.GET, "/lists/"+listID+"/accounts", new TypeToken<>(){});
		if(!TextUtils.isEmpty(maxID))
			addQueryParameter("max_id", maxID);
		addQueryParameter("limit", String.valueOf(limit));
	}
}
