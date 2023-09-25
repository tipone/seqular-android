package org.joinmastodon.android.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.joinmastodon.android.E;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.requests.filters.CreateFilter;
import org.joinmastodon.android.api.requests.filters.DeleteFilter;
import org.joinmastodon.android.api.requests.filters.GetFilters;
import org.joinmastodon.android.api.requests.tags.GetHashtag;
import org.joinmastodon.android.api.requests.tags.SetHashtagFollowed;
import org.joinmastodon.android.api.requests.timelines.GetHashtagTimeline;
import org.joinmastodon.android.events.HashtagUpdatedEvent;
import org.joinmastodon.android.fragments.settings.EditFilterFragment;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.FilterAction;
import org.joinmastodon.android.model.FilterContext;
import org.joinmastodon.android.model.FilterKeyword;
import org.joinmastodon.android.model.Hashtag;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.model.TimelineDefinition;
import org.joinmastodon.android.ui.utils.UiUtils;
import org.joinmastodon.android.utils.StatusFilterPredicate;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.V;

public class HashtagTimelineFragment extends PinnableStatusListFragment {
	private String hashtag;
	private List<String> any;
	private List<String> all;
	private List<String> none;
	private boolean following;
	private boolean localOnly;
	private MenuItem followButton;
	private MenuItem muteButton;
	private Optional<Filter> filter = Optional.empty();

	@Override
	protected boolean wantsComposeButton() {
		return true;
	}


	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		updateTitle(getArguments().getString("hashtag"));
		following=getArguments().getBoolean("following", false);
		localOnly=getArguments().getBoolean("localOnly", false);
		any=getArguments().getStringArrayList("any");
		all=getArguments().getStringArrayList("all");
		none=getArguments().getStringArrayList("none");
		setHasOptionsMenu(true);
	}

	private void updateTitle(String hashtagName) {
		hashtag = hashtagName;
		setTitle('#'+hashtag);
	}

	private void updateFollowingState(boolean newFollowing) {
		this.following = newFollowing;
		followButton.setTitle(getString(newFollowing ? R.string.unfollow_user : R.string.follow_user, "#" + hashtag));
		followButton.setIcon(newFollowing ? R.drawable.ic_fluent_person_delete_24_filled : R.drawable.ic_fluent_person_add_24_regular);
		E.post(new HashtagUpdatedEvent(hashtag, following));
	}

	private void updateMuteState(boolean newMute) {
		muteButton.setTitle(getString(newMute ? R.string.unmute_user : R.string.mute_user, "#" + hashtag));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.hashtag_timeline, menu);
		super.onCreateOptionsMenu(menu, inflater);
		followButton = menu.findItem(R.id.follow_hashtag);
		updateFollowingState(following);
		muteButton = menu.findItem(R.id.mute_hashtag);
		updateMuteState(filter.isPresent());
		new GetHashtag(hashtag).setCallback(new Callback<>() {
			@Override
			public void onSuccess(Hashtag hashtag) {
				if (getActivity() == null) return;
				updateTitle(hashtag.name);
				updateFollowingState(hashtag.following);
			}

			@Override
			public void onError(ErrorResponse error) {
				error.showToast(getActivity());
			}
		}).exec(accountID);

		new GetFilters().setCallback(new Callback<>() {
			@Override
			public void onSuccess(List<Filter> filters) {
				if (getActivity() == null) return;
				filter=filters.stream().filter(filter->filter.title.equals("#"+hashtag)).findAny();
				updateMuteState(filter.isPresent());
			}

			@Override
			public void onError(ErrorResponse error) {
				error.showToast(getActivity());
			}
		}).exec(accountID);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item)) return true;
		if (item.getItemId() == R.id.follow_hashtag) {
			updateFollowingState(!following);
			getToolbar().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
			new SetHashtagFollowed(hashtag, following).setCallback(new Callback<>() {
				@Override
				public void onSuccess(Hashtag i) {
					if (getActivity() == null) return;
					if (i.following == following) Toast.makeText(getActivity(), getString(i.following ? R.string.followed_user : R.string.unfollowed_user, "#" + i.name), Toast.LENGTH_SHORT).show();
					updateFollowingState(i.following);
				}

				@Override
				public void onError(ErrorResponse error) {
					error.showToast(getActivity());
					updateFollowingState(!following);
				}
			}).exec(accountID);
			return true;
		} else if (item.getItemId() == R.id.mute_hashtag) {
			showMuteDialog(filter.isPresent());
			return true;
		}
		return false;
	}

	private void showMuteDialog(boolean mute) {
		UiUtils.showConfirmationAlert(getContext(),
										   mute ? R.string.mo_unmute_hashtag : R.string.mo_mute_hashtag,
										   mute ? R.string.mo_confirm_to_unmute_hashtag : R.string.mo_confirm_to_mute_hashtag,
										   mute ? R.string.do_unmute : R.string.do_mute,
										   mute ? R.drawable.ic_fluent_speaker_2_28_regular : R.drawable.ic_fluent_speaker_off_28_regular,
				mute ? this::unmuteHashtag : this::muteHashtag
		);
	}
	private void unmuteHashtag() {
		//safe to get, this only called if filter is present
		new DeleteFilter(filter.get().id).setCallback(new Callback<>(){
			@Override
			public void onSuccess(Void result){
				updateMuteState(false);
			}

			@Override
			public void onError(ErrorResponse error){
				error.showToast(getContext());
			}
		}).exec(accountID);
	}

	private void muteHashtag() {
		FilterKeyword hashtagFilter=new FilterKeyword();
		hashtagFilter.wholeWord=true;
		hashtagFilter.keyword="#"+hashtag;
		new CreateFilter("#"+hashtag, EnumSet.of(FilterContext.HOME), FilterAction.HIDE, 0 , List.of(hashtagFilter)).setCallback(new Callback<>(){
			@Override
			public void onSuccess(Filter result){
				filter=Optional.of(result);
				updateMuteState(true);
			}

			@Override
			public void onError(ErrorResponse error){
				error.showToast(getContext());
			}
		}).exec(accountID);
	}



	@Override
	protected TimelineDefinition makeTimelineDefinition() {
		return TimelineDefinition.ofHashtag(hashtag);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetHashtagTimeline(hashtag, offset==0 ? null : getMaxID(), null, count, any, all, none, localOnly, getLocalPrefs().timelineReplyVisibility)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Status> result){
						if (getActivity() == null) return;
						result=result.stream().filter(new StatusFilterPredicate(accountID, getFilterContext())).collect(Collectors.toList());
						onDataLoaded(result, !result.isEmpty());
					}
				})
				.exec(accountID);
	}

	@Override
	protected void onShown(){
		super.onShown();
		if(!getArguments().getBoolean("noAutoLoad") && !loaded && !dataLoading)
			loadData();
	}

	@Override
	public boolean onFabLongClick(View v) {
		return UiUtils.pickAccountForCompose(getActivity(), accountID, '#'+hashtag+' ');
	}

	@Override
	public void onFabClick(View v){
		Bundle args=new Bundle();
		args.putString("account", accountID);
		args.putString("prefilledText", '#'+hashtag+' ');
		Nav.go(getActivity(), ComposeFragment.class, args);
	}

	@Override
	protected void onSetFabBottomInset(int inset){
		((ViewGroup.MarginLayoutParams) fab.getLayoutParams()).bottomMargin=V.dp(16)+inset;
	}

	@Override
	protected FilterContext getFilterContext() {
		return FilterContext.PUBLIC;
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return base.path((isInstanceAkkoma() ? "/tag/" : "/tags") + hashtag).build();
	}
}
