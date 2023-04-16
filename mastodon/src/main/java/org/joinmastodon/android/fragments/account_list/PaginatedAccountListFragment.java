package org.joinmastodon.android.fragments.account_list;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.api.requests.HeaderPaginationRequest;
import org.joinmastodon.android.model.Account;
import org.joinmastodon.android.model.HeaderPaginationList;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.util.stream.Collectors;

import me.grishka.appkit.api.SimpleCallback;

public abstract class PaginatedAccountListFragment extends BaseAccountListFragment{
	private String nextMaxID;

	protected Account targetAccount;

	public abstract HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count);

	public abstract HeaderPaginationRequest<Account> onCreateRemoteRequest(String id, String maxID, int count);

	@Override
	protected void doLoadData(int offset, int count){
		if(GlobalUserPreferences.loadRemoteAccountFollowers){
			if ((this instanceof FollowingListFragment || this instanceof FollowerListFragment) && targetAccount != null){
				UiUtils.lookupRemoteAccount(getContext(), targetAccount, accountID, null, account -> {
					if(account != null){
						currentRequest=onCreateRemoteRequest(account.id, offset==0 ? null : nextMaxID, count)
								.setCallback(new SimpleCallback<>(this){
									@Override
									public void onSuccess(HeaderPaginationList<Account> result){
										if(result.nextPageUri!=null)
											nextMaxID=result.nextPageUri.getQueryParameter("max_id");
										else
											nextMaxID=null;
										result.stream().forEach(remoteAccount -> {
											remoteAccount.reloadWhenClicked = true;
										});
										if (getActivity() == null) return;
										onDataLoaded(result.stream().map(AccountItem::new).collect(Collectors.toList()), false);
									}
								})
								.execNoAuth(targetAccount.getDomain());
					} else {
						currentRequest=onCreateRequest(offset==0 ? null : nextMaxID, count)
								.setCallback(new SimpleCallback<>(this){
									@Override
									public void onSuccess(HeaderPaginationList<Account> result){
										if(result.nextPageUri!=null)
											nextMaxID=result.nextPageUri.getQueryParameter("max_id");
										else
											nextMaxID=null;
										if (getActivity() == null) return;
										onDataLoaded(result.stream().map(AccountItem::new).collect(Collectors.toList()), nextMaxID!=null);
									}
								})
								.exec(accountID);
					}
				});
			}
		} else {
			currentRequest=onCreateRequest(offset==0 ? null : nextMaxID, count)
					.setCallback(new SimpleCallback<>(this){
						@Override
						public void onSuccess(HeaderPaginationList<Account> result){
							if(result.nextPageUri!=null)
								nextMaxID=result.nextPageUri.getQueryParameter("max_id");
							else
								nextMaxID=null;
							if (getActivity() == null) return;
							onDataLoaded(result.stream().map(AccountItem::new).collect(Collectors.toList()), nextMaxID!=null);
						}
					})
					.exec(accountID);
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		if(!loaded && !dataLoading)
			loadData();
	}
}
