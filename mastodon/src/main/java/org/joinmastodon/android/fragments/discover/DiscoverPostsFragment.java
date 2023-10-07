package org.joinmastodon.android.fragments.discover;

import android.net.Uri;
import android.os.Bundle;

import org.joinmastodon.android.api.requests.trends.GetTrendingStatuses;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.fragments.StatusListFragment;
import org.joinmastodon.android.model.FilterContext;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.utils.DiscoverInfoBannerHelper;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.MergeRecyclerAdapter;

public class DiscoverPostsFragment extends StatusListFragment{
	private DiscoverInfoBannerHelper bannerHelper;
	private int offset;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		bannerHelper=new DiscoverInfoBannerHelper(DiscoverInfoBannerHelper.BannerType.TRENDING_POSTS, accountID);
	}

	@Override
	protected void doLoadData(int o, int count){
		if(refreshing) offset=0;
		currentRequest=new GetTrendingStatuses(offset, count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Status> result){
						if(getActivity()==null) return;
						boolean empty=result.isEmpty();
						offset+=result.size();
						AccountSessionManager.get(accountID).filterStatuses(result, getFilterContext());
						onDataLoaded(result, !empty);
						bannerHelper.onBannerBecameVisible();
					}
				}).exec(accountID);
	}

	@Override
	protected RecyclerView.Adapter<?> getAdapter(){
		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		bannerHelper.maybeAddBanner(list, adapter);
		adapter.addAdapter(super.getAdapter());
		return adapter;
	}

	@Override
	protected FilterContext getFilterContext() {
		return FilterContext.PUBLIC;
	}

	@Override
	public Uri getWebUri(Uri.Builder base){
		return isInstanceAkkoma() ? null : base.path("/explore/posts").build();
	}
}
