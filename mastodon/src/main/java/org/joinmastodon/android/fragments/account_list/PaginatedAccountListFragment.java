package org.joinmastodon.android.fragments.account_list;

import android.app.Activity;
import android.widget.Toast;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.requests.HeaderPaginationRequest;
import org.joinmastodon.android.api.requests.search.GetSearchResults;
import org.joinmastodon.android.model.Account;
import org.joinmastodon.android.model.HeaderPaginationList;
import org.joinmastodon.android.model.SearchResults;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;

public abstract class PaginatedAccountListFragment extends BaseAccountListFragment{
	private String nextMaxID;

	public abstract Status getTargetStatus();

	public abstract Account getTargetAccount();

	public abstract HeaderPaginationRequest<Account> onCreateRemoteRequest(String id, String maxID, int count);

	public abstract HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count);

	@Override
	protected void doLoadData(int offset, int count){
		if(!GlobalUserPreferences.relocatePublishButton){
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
		} else {
			Status targetStatus = getTargetStatus();
			Account targetAccount = getTargetAccount();

			if(targetAccount != null){
				UiUtils.lookupRemoteAccount(getContext(), targetAccount, accountID, null, account -> {
					Pattern pattern = Pattern.compile("(?<=\\/\\/)([^\\/]+)(?=\\/@)");
					Matcher matcher = pattern.matcher(account.url);
					String domain = null;
					if(matcher.find()){
						domain = matcher.group(1);
					}
					currentRequest=onCreateRemoteRequest(account.id, offset==0 ? null : nextMaxID, count)
							.setCallback(new SimpleCallback<>(this) {
								@Override
								public void onSuccess(HeaderPaginationList<Account> result) {
									if(result.nextPageUri!=null)
										nextMaxID=result.nextPageUri.getQueryParameter("max_id");
									else
										nextMaxID=null;
									if (getActivity() == null) return;
									onDataLoaded(result.stream().map(AccountItem::new).collect(Collectors.toList()), nextMaxID!=null);
								}
							}).execNoAuth(domain);
				});
				return;
			}

			if(targetStatus != null){
				UiUtils.lookupRemoteStatus(getContext(), targetStatus, accountID, null, status -> {
					Pattern pattern = Pattern.compile("(?<=\\/\\/)([^\\/]+)(?=\\/@)");
					Matcher matcher = pattern.matcher(status.url);
					String domain = null;
					if(matcher.find()){
						domain = matcher.group(1);
					}
					currentRequest=onCreateRemoteRequest(status.id, offset==0 ? null : nextMaxID, count)
							.setCallback(new SimpleCallback<>(this) {
								@Override
								public void onSuccess(HeaderPaginationList<Account> result) {
									if(result.nextPageUri!=null)
										nextMaxID=result.nextPageUri.getQueryParameter("max_id");
									else
										nextMaxID=null;
									if (getActivity() == null) return;
									onDataLoaded(result.stream().map(AccountItem::new).collect(Collectors.toList()), nextMaxID!=null);
								}
							}).execNoAuth(domain);
				});
			}
		}

	}

	@Override
	public void onResume(){
		super.onResume();
		if(!loaded && !dataLoading)
			loadData();
	}
}
