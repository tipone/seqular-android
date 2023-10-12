package org.joinmastodon.android.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.api.requests.markers.SaveMarkers;
import org.joinmastodon.android.api.requests.timelines.GetHomeTimeline;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.CacheablePaginatedResponse;
import org.joinmastodon.android.model.FilterContext;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.model.TimelineMarkers;
import org.joinmastodon.android.ui.displayitems.GapStatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.StatusDisplayItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;

public class HomeTimelineFragment extends StatusListFragment {
	private HomeTabFragment parent;
	private String maxID;
	private String lastSavedMarkerID;

	@Override
	protected boolean wantsComposeButton() {
		return true;
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		if (getParentFragment() instanceof HomeTabFragment home) parent = home;
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		AccountSessionManager.getInstance()
				.getAccount(accountID).getCacheController()
				.getHomeTimeline(offset>0 ? maxID : null, count, refreshing, new SimpleCallback<>(this){
					@Override
					public void onSuccess(CacheablePaginatedResponse<List<Status>> result){
						if(getActivity()==null) return;
						boolean empty=result.items.isEmpty();
						maxID=result.maxID;
						AccountSessionManager.get(accountID).filterStatuses(result.items, getFilterContext());
						onDataLoaded(result.items, !empty);
						if(result.isFromCache())
							loadNewPosts();
					}
				});
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);

		list.addOnScrollListener(new RecyclerView.OnScrollListener(){
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy){
				if(parent != null && parent.isNewPostsBtnShown() && list.getChildAdapterPosition(list.getChildAt(0))<=getMainAdapterOffset()){
					parent.hideNewPostsButton();
				}
			}
		});
	}

	@Override
	protected void onShown(){
		super.onShown();
		if(!getArguments().getBoolean("noAutoLoad")){
			if(!loaded && !dataLoading){
				loadData();
			}else if(!dataLoading){
				loadNewPosts();
			}
		}
	}

	@Override
	protected void onHidden(){
		super.onHidden();
		if(!data.isEmpty()){
			String topPostID=displayItems.get(Math.max(0, list.getChildAdapterPosition(list.getChildAt(0))-getMainAdapterOffset())).parentID;
			if(!topPostID.equals(lastSavedMarkerID)){
				lastSavedMarkerID=topPostID;
				new SaveMarkers(topPostID, null)
						.setCallback(new Callback<>(){
							@Override
							public void onSuccess(TimelineMarkers result){
							}

							@Override
							public void onError(ErrorResponse error){
								lastSavedMarkerID=null;
							}
						})
						.exec(accountID);
			}
		}
	}

	public void onStatusCreated(Status status){
		prependItems(Collections.singletonList(status), true);
	}

	private void loadNewPosts(){
		if (!GlobalUserPreferences.loadNewPosts) return;
		dataLoading=true;
		// The idea here is that we request the timeline such that if there are fewer than `limit` posts,
		// we'll get the currently topmost post as last in the response. This way we know there's no gap
		// between the existing and newly loaded parts of the timeline.
		String sinceID=data.size()>1 ? data.get(1).id : "1";
		currentRequest=new GetHomeTimeline(null, null, 20, sinceID, getLocalPrefs().timelineReplyVisibility)
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(List<Status> result){
						currentRequest=null;
						dataLoading=false;
						if(result.isEmpty() || getActivity()==null)
							return;
						Status last=result.get(result.size()-1);
						List<Status> toAdd;
						if(!data.isEmpty() && last.id.equals(data.get(0).id)){ // This part intersects with the existing one
							toAdd=new ArrayList<>(result.subList(0, result.size()-1)); // Remove the already known last post
						}else{
							result.get(result.size()-1).hasGapAfter=true;
							toAdd=result;
						}
						List<String> existingIds=data.stream().map(Status::getID).collect(Collectors.toList());
						toAdd.removeIf(s->existingIds.contains(s.getID()));
						List<Status> toAddUnfiltered=new ArrayList<>(toAdd);
						AccountSessionManager.get(accountID).filterStatuses(toAdd, getFilterContext());
						if(!toAdd.isEmpty()){
							prependItems(toAdd, true);
							if(parent != null && GlobalUserPreferences.showNewPostsButton) parent.showNewPostsButton();
						}
						if(toAddUnfiltered.isEmpty())
							AccountSessionManager.getInstance().getAccount(accountID).getCacheController().putHomeTimeline(toAddUnfiltered, false);
					}

					@Override
					public void onError(ErrorResponse error){
						currentRequest=null;
						dataLoading=false;
					}
				})
				.exec(accountID);

		if (parent.getParentFragment() instanceof HomeFragment homeFragment) {
			homeFragment.reloadNotificationsForUnreadCount();
		}
	}

	@Override
	public void onGapClick(GapStatusDisplayItem.Holder item, boolean downwards){
		if(dataLoading)
			return;
		GapStatusDisplayItem gap=item.getItem();
		gap.loading=true;
		dataLoading=true;

		String maxID = null;
		String minID = null;
		if (downwards) {
			maxID = item.getItemID();
		} else {
			int gapPos=displayItems.indexOf(gap);
			StatusDisplayItem nextItem=displayItems.get(gapPos + 1);
			minID=nextItem.parentID;
		}
		currentRequest=new GetHomeTimeline(maxID, minID, 20, null, getLocalPrefs().timelineReplyVisibility)
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(List<Status> result){
						currentRequest=null;
						dataLoading=false;
						if(getActivity()==null)
							return;
						int gapPos=displayItems.indexOf(gap);
						if(gapPos==-1)
							return;
						AccountSessionManager.get(accountID).filterStatuses(result, getFilterContext());
						if(result.isEmpty()){
							displayItems.remove(gapPos);
							adapter.notifyItemRemoved(getMainAdapterOffset()+gapPos);
							Status gapStatus=getStatusByID(gap.parentID);
							if(gapStatus!=null){
								gapStatus.hasGapAfter=false;
								AccountSessionManager.getInstance().getAccount(accountID).getCacheController().putHomeTimeline(Collections.singletonList(gapStatus), false);
							}
						}else{
							if(downwards) {
								Set<String> idsBelowGap=new HashSet<>();
								boolean belowGap=false;
								int gapPostIndex=0;
								for(Status s:data){
									if(belowGap){
										idsBelowGap.add(s.id);
									}else if(s.id.equals(gap.parentID)){
										belowGap=true;
										s.hasGapAfter=false;
										AccountSessionManager.getInstance().getAccount(accountID).getCacheController().putHomeTimeline(Collections.singletonList(s), false);
									}else{
										gapPostIndex++;
									}
								}
								int endIndex=0;
								for(Status s:result){
									endIndex++;
									if(idsBelowGap.contains(s.id))
										break;
								}
								if(endIndex==result.size()){
									result.get(result.size()-1).hasGapAfter=true;
								}else{
									result=result.subList(0, endIndex);
								}
								AccountSessionManager.get(accountID).filterStatuses(result, FilterContext.HOME);
								List<StatusDisplayItem> targetList=displayItems.subList(gapPos, gapPos+1);
								targetList.clear();
								List<Status> insertedPosts=data.subList(gapPostIndex+1, gapPostIndex+1);
								for(Status s:result){
									if(idsBelowGap.contains(s.id))
										break;
									targetList.addAll(buildDisplayItems(s));
									insertedPosts.add(s);
								}
								if(targetList.isEmpty()){
									// oops. We didn't add new posts, but at least we know there are none.
									adapter.notifyItemRemoved(getMainAdapterOffset()+gapPos);
								}else{
									adapter.notifyItemChanged(getMainAdapterOffset()+gapPos);
									adapter.notifyItemRangeInserted(getMainAdapterOffset()+gapPos+1, targetList.size()-1);
								}
								AccountSessionManager.getInstance().getAccount(accountID).getCacheController().putHomeTimeline(insertedPosts, false);
							} else {
								String aboveGapID = gap.parentID;
								int gapPostIndex = 0;
								for (;gapPostIndex<data.size();gapPostIndex++){
									if (Objects.equals(aboveGapID, data.get(gapPostIndex).id)) {
										break;
									}
								}
								// find if there's an overlap between the new data and the current data
								int indexOfGapInResponse = 0;
								for (;indexOfGapInResponse<result.size();indexOfGapInResponse++){
									if (Objects.equals(aboveGapID, result.get(indexOfGapInResponse).id)) {
										break;
									}
								}
								// there is an overlap between new and current data
								List<StatusDisplayItem> targetList=displayItems.subList(gapPos, gapPos+1);
								if(indexOfGapInResponse<result.size()){
									result=result.subList(indexOfGapInResponse+1,result.size());
									Optional<Status> gapStatus=data.stream()
											.filter(s->Objects.equals(s.id, gap.parentID))
											.findFirst();
									if (gapStatus.isPresent()) {
										gapStatus.get().hasGapAfter=false;
										AccountSessionManager.getInstance().getAccount(accountID).getCacheController().putHomeTimeline(Collections.singletonList(gapStatus.get()), false);
									}
									targetList.clear();
								} else {
									gap.loading=false;
								}
								List<Status> insertedPosts=data.subList(gapPostIndex+1, gapPostIndex+1);
								for(Status s:result){
									targetList.addAll(buildDisplayItems(s));
									insertedPosts.add(s);
								}
								AccountSessionManager.get(accountID).filterStatuses(insertedPosts, FilterContext.HOME);
								if(targetList.isEmpty()){
									// oops. We didn't add new posts, but at least we know there are none.
									adapter.notifyItemRemoved(getMainAdapterOffset()+gapPos);
								}else{
									adapter.notifyItemChanged(getMainAdapterOffset()+gapPos);
									adapter.notifyItemRangeInserted(getMainAdapterOffset()+gapPos+1, targetList.size()-1);
								}
								list.scrollToPosition(getMainAdapterOffset()+gapPos+targetList.size());
								AccountSessionManager.getInstance().getAccount(accountID).getCacheController().putHomeTimeline(insertedPosts, false);
							}
						}
					}

					@Override
					public void onError(ErrorResponse error){
						currentRequest=null;
						dataLoading=false;
						gap.loading=false;
						Activity a=getActivity();
						if(a!=null){
							error.showToast(a);
							int gapPos=displayItems.indexOf(gap);
							if(gapPos>=0)
								adapter.notifyItemChanged(gapPos);
						}
					}
				})
				.exec(accountID);

	}

	@Override
	public void onRefresh(){
		if(currentRequest!=null){
			currentRequest.cancel();
			currentRequest=null;
			dataLoading=false;
		}
		if (parent != null) parent.hideNewPostsButton();
		super.onRefresh();
	}

	@Override
	protected boolean shouldRemoveAccountPostsWhenUnfollowing(){
		return true;
	}

	@Override
	protected FilterContext getFilterContext() {
		return FilterContext.HOME;
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return base.path("/").build();
	}
}
