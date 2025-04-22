package net.seqular.network.fragments.account_list;

import android.os.Bundle;

import net.seqular.network.R;
import net.seqular.network.api.requests.accounts.SearchAccounts;
import net.seqular.network.model.Account;

import java.util.List;

import me.grishka.appkit.api.SimpleCallback;

public class AddListMembersFragment extends AccountSearchFragment{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		dataLoaded();
	}

	@Override
	protected void doLoadData(int offset, int count){
		refreshing=true;
		currentRequest=new SearchAccounts(currentQuery, 0, 0, false, true)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Account> result){
						AddListMembersFragment.this.onSuccess(result);
					}
				})
				.exec(accountID);
	}

	@Override
	protected String getSearchViewPlaceholder(){
		return getString(R.string.search_among_people_you_follow);
	}
}
