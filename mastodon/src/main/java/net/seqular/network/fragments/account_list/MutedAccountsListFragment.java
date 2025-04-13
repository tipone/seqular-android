package net.seqular.network.fragments.account_list;

import android.net.Uri;
import android.os.Bundle;

import net.seqular.network.R;
import net.seqular.network.api.requests.HeaderPaginationRequest;
import net.seqular.network.api.requests.accounts.GetAccountMutes;
import net.seqular.network.model.Account;
import net.seqular.network.ui.viewholders.AccountViewHolder;

public class MutedAccountsListFragment extends AccountRelatedAccountListFragment{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(R.string.sk_muted_accounts);
	}

	@Override
	public HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count){
		return new GetAccountMutes(maxID, count);
	}

	@Override
	protected void onConfigureViewHolder(AccountViewHolder holder){
		super.onConfigureViewHolder(holder);
		holder.setStyle(AccountViewHolder.AccessoryType.NONE, false);
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return super.getWebUri(base).buildUpon()
				.appendPath("/mutes").build();
	}
}
