package net.seqular.network.api.requests.lists;

import net.seqular.network.api.ResultlessMastodonAPIRequest;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import okhttp3.FormBody;

public class RemoveAccountsFromList extends ResultlessMastodonAPIRequest{
	public RemoveAccountsFromList(String listID, Collection<String> accountIDs){
		super(HttpMethod.DELETE, "/lists/"+listID+"/accounts");
		FormBody.Builder builder=new FormBody.Builder(StandardCharsets.UTF_8);
		for(String id:accountIDs){
			builder.add("account_ids[]", id);
		}
		setRequestBody(builder.build());
	}
}
