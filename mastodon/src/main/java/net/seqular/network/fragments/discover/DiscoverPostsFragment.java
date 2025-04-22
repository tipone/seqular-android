package net.seqular.network.fragments.discover;

import android.net.Uri;
import android.os.Bundle;

import net.seqular.network.api.requests.trends.GetTrendingStatuses;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.fragments.StatusListFragment;
import net.seqular.network.model.FilterContext;
import net.seqular.network.model.Status;
import net.seqular.network.ui.utils.DiscoverInfoBannerHelper;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.MergeRecyclerAdapter;

public class DiscoverPostsFragment extends StatusListFragment{
	private DiscoverInfoBannerHelper bannerHelper;
	private int realOffset=0;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		bannerHelper=new DiscoverInfoBannerHelper(DiscoverInfoBannerHelper.BannerType.TRENDING_POSTS, accountID);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetTrendingStatuses(offset==0 ? 0 : realOffset, count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Status> result){
						if(getActivity()==null) return;
						realOffset+=result.size();
						AccountSessionManager.get(accountID).filterStatuses(result, getFilterContext());
						onDataLoaded(result, !result.isEmpty());
						bannerHelper.onBannerBecameVisible();
					}
				}).exec(accountID);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
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
