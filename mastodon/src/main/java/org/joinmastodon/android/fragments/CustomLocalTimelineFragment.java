package org.joinmastodon.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.joinmastodon.android.E;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.requests.tags.GetHashtag;
import org.joinmastodon.android.api.requests.tags.SetHashtagFollowed;
import org.joinmastodon.android.api.requests.timelines.GetHashtagTimeline;
import org.joinmastodon.android.api.requests.timelines.GetPublicTimeline;
import org.joinmastodon.android.events.HashtagUpdatedEvent;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.Hashtag;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.model.TimelineDefinition;
import org.joinmastodon.android.ui.utils.UiUtils;
import org.joinmastodon.android.utils.StatusFilterPredicate;

import java.util.List;
import java.util.stream.Collectors;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.V;

public class CustomLocalTimelineFragment extends PinnableStatusListFragment {
//    private String name;
    private String domain;

    private String maxID;
    @Override
    protected boolean withComposeButton() {
        return false;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        domain=getArguments().getString("domain");
        updateTitle(getArguments().getString("domain"));

        setHasOptionsMenu(true);
    }

    private void updateTitle(String domain) {
        this.domain = domain;
        setTitle(domain);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.hashtag_timeline, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected TimelineDefinition makeTimelineDefinition() {
        return TimelineDefinition.ofCustomLocalTimeline(domain);
    }

    @Override
    protected void doLoadData(int offset, int count){
        currentRequest=new GetPublicTimeline(true, false, refreshing ? null : maxID, count)
                .setCallback(new SimpleCallback<>(this){
                    @Override
                    public void onSuccess(List<Status> result){
                        if(!result.isEmpty())
                            maxID=result.get(result.size()-1).id;
                        if (getActivity() == null) return;
                        result=result.stream().filter(new StatusFilterPredicate(accountID, Filter.FilterContext.PUBLIC)).collect(Collectors.toList());
                        onDataLoaded(result, !result.isEmpty());
                    }
                })
                .execNoAuth("fosstodon.org");
    }

    @Override
    protected void onShown(){
        super.onShown();
        if(!getArguments().getBoolean("noAutoLoad") && !loaded && !dataLoading)
            loadData();
    }
}
