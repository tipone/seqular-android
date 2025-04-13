package net.seqular.network.fragments.discover;

import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import net.seqular.network.api.requests.timelines.GetBubbleTimeline;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.fragments.StatusListFragment;
import net.seqular.network.model.FilterContext;
import net.seqular.network.model.Status;
import net.seqular.network.ui.utils.DiscoverInfoBannerHelper;

import java.util.List;

import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.MergeRecyclerAdapter;

public class BubbleTimelineFragment extends StatusListFragment {
    private DiscoverInfoBannerHelper bannerHelper;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		bannerHelper=new DiscoverInfoBannerHelper(DiscoverInfoBannerHelper.BannerType.BUBBLE_TIMELINE, accountID);
	}


	@Override
    protected boolean wantsComposeButton() {
        return true;
    }

	@Override
    protected void doLoadData(int offset, int count){
        currentRequest=new GetBubbleTimeline(getMaxID(), count, getLocalPrefs().timelineReplyVisibility)
                .setCallback(new SimpleCallback<>(this){
                    @Override
                    public void onSuccess(List<Status> result){
						if(getActivity()==null) return;
						boolean more=applyMaxID(result);
						AccountSessionManager.get(accountID).filterStatuses(result, getFilterContext());
						onDataLoaded(result, more);
						bannerHelper.onBannerBecameVisible();
                    }
                })
                .exec(accountID);
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
    public Uri getWebUri(Uri.Builder base) {
        return isInstanceAkkoma() ? base.path("/main/bubble").build() : null;
    }
}
