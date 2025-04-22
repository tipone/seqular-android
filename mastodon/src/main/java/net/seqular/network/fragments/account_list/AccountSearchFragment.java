package net.seqular.network.fragments.account_list;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import net.seqular.network.R;
import net.seqular.network.api.requests.search.GetSearchResults;
import net.seqular.network.model.Account;
import net.seqular.network.model.SearchResults;
import net.seqular.network.model.viewmodel.AccountViewModel;
import net.seqular.network.ui.SearchViewHelper;
import net.seqular.network.ui.utils.UiUtils;
import net.seqular.network.ui.viewholders.AccountViewHolder;
import org.parceler.Parcels;

import java.util.List;
import java.util.stream.Collectors;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;

public class AccountSearchFragment extends BaseAccountListFragment{
	protected String currentQuery;
	private boolean resultDelivered;
	private SearchViewHelper searchViewHelper;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRefreshEnabled(false);
		setEmptyText("");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		searchViewHelper=new SearchViewHelper(getActivity(), getToolbarContext(), getSearchViewPlaceholder());
		searchViewHelper.setListeners(this::onQueryChanged, null);
		searchViewHelper.addDivider(contentView);
		super.onViewCreated(view, savedInstanceState);

		view.setBackgroundResource(R.drawable.bg_m3_surface3);
		int color=UiUtils.alphaBlendThemeColors(getActivity(), R.attr.colorM3Surface, R.attr.colorM3Primary, 0.11f);
		setStatusBarColor(color);
		setNavigationBarColor(color);
	}

	@Override
	protected void doLoadData(int offset, int count){
		refreshing=true;
		currentRequest=new GetSearchResults(currentQuery, GetSearchResults.Type.ACCOUNTS, false, null, 0, 0)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(SearchResults result){
						AccountSearchFragment.this.onSuccess(result.accounts);
					}
				})
				.exec(accountID);
	}

	protected void onSuccess(List<Account> result){
		setEmptyText(R.string.no_search_results);
		onDataLoaded(result.stream().map(a->new AccountViewModel(a, accountID)).collect(Collectors.toList()), false);
	}

	protected String getSearchViewPlaceholder(){
		return getString(R.string.search_hint);
	}

	@Override
	protected void onUpdateToolbar(){
		super.onUpdateToolbar();
		searchViewHelper.install(getToolbar());
	}

	@Override
	protected boolean wantsElevationOnScrollEffect(){
		return false;
	}

	@Override
	protected void onConfigureViewHolder(AccountViewHolder holder){
		super.onConfigureViewHolder(holder);
		holder.setOnClickListener(this::onItemClick);
		holder.setStyle(AccountViewHolder.AccessoryType.NONE, false);
	}

	private void onItemClick(AccountViewHolder holder){
		if(resultDelivered)
			return;

		resultDelivered=true;
		Bundle res=new Bundle();
		res.putParcelable("selectedAccount", Parcels.wrap(holder.getItem().account));
		setResult(true, res);
		Nav.finish(this, false);
	}

	private void onQueryChanged(String q){
		currentQuery=q;
		if(currentRequest!=null){
			currentRequest.cancel();
			currentRequest=null;
		}
		if(!TextUtils.isEmpty(currentQuery))
			loadData();
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return null;
	}
}
