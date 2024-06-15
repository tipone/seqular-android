package org.joinmastodon.android.fragments.discover;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.joinmastodon.android.R;
import org.joinmastodon.android.api.requests.timelines.GetTrendingLinksTimeline;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.fragments.HomeTabFragment;
import org.joinmastodon.android.fragments.StatusListFragment;
import org.joinmastodon.android.model.Card;
import org.joinmastodon.android.model.FilterContext;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.utils.UiUtils;
import org.parceler.Parcels;

import java.util.List;

import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.MergeRecyclerAdapter;
import me.grishka.appkit.utils.SingleViewRecyclerAdapter;
import me.grishka.appkit.utils.V;

//TODO: replace this implementation when upstream implements their own design
public class DiscoverTrendingLinkTimelineFragment extends StatusListFragment{
	private Card trendingLink;
	private TextView headerTitle, headerSubtitle;
	private Button openLinkButton;
	private boolean toolbarContentVisible;

	private Menu optionsMenu;
	private MenuInflater optionsMenuInflater;

	@Override
	protected boolean wantsComposeButton() {
		return true;
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		trendingLink=Parcels.unwrap(getArguments().getParcelable("trendingLink"));
		setTitle(trendingLink.title);
		setHasOptionsMenu(true);
	}


	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetTrendingLinksTimeline(trendingLink.url, getMaxID(), null, count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Status> result){
						if(getActivity()==null) return;
						boolean more=applyMaxID(result);
						AccountSessionManager.get(accountID).filterStatuses(result, getFilterContext());
						onDataLoaded(result, more);
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
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);

		if(getParentFragment() instanceof HomeTabFragment) return;

		list.addOnScrollListener(new RecyclerView.OnScrollListener(){
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy){
				View topChild=recyclerView.getChildAt(0);
				int firstChildPos=recyclerView.getChildAdapterPosition(topChild);
				float newAlpha=firstChildPos>0 ? 1f : Math.min(1f, -topChild.getTop()/(float)headerTitle.getHeight());
				toolbarTitleView.setAlpha(newAlpha);
				boolean newToolbarVisibility=newAlpha>0.5f;
				if(newToolbarVisibility!=toolbarContentVisible){
					toolbarContentVisible=newToolbarVisibility;
					createOptionsMenu();
				}
			}
		});
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
		//TODO: add URL link once web version implements a UI
		return base.path("/explore/links").build();
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		View header=getActivity().getLayoutInflater().inflate(R.layout.header_trending_link_timeline, list, false);
		headerTitle=header.findViewById(R.id.title);
		headerSubtitle=header.findViewById(R.id.subtitle);
		openLinkButton=header.findViewById(R.id.profile_action_btn);

		headerTitle.setText(trendingLink.title);
		openLinkButton.setVisibility(View.GONE);
		openLinkButton.setOnClickListener(v->{
			if(trendingLink==null)
				return;
			openLink();
		});
		updateHeader();

		MergeRecyclerAdapter mergeAdapter=new MergeRecyclerAdapter();
		if(!(getParentFragment() instanceof HomeTabFragment)){
			mergeAdapter.addAdapter(new SingleViewRecyclerAdapter(header));
		}
		mergeAdapter.addAdapter(super.getAdapter());
		return mergeAdapter;
	}

	@Override
	protected int getMainAdapterOffset(){
		return 1;
	}

	private void createOptionsMenu(){
		optionsMenu.clear();
		optionsMenuInflater.inflate(R.menu.trending_links_timeline, optionsMenu);
		MenuItem openLinkMenuItem=optionsMenu.findItem(R.id.open_link);
		openLinkMenuItem.setVisible(toolbarContentVisible);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.trending_links_timeline, menu);
		super.onCreateOptionsMenu(menu, inflater);
		optionsMenu=menu;
		optionsMenuInflater=inflater;
		createOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (super.onOptionsItemSelected(item)) return true;
		if (item.getItemId() == R.id.open_link && trendingLink!=null) {
			openLink();
		}
		return true;
	}

	@Override
	protected void onUpdateToolbar(){
		super.onUpdateToolbar();
		toolbarTitleView.setAlpha(toolbarContentVisible ? 1f : 0f);
		createOptionsMenu();
	}

	private void updateHeader(){
		if(trendingLink==null || getActivity()==null)
			return;
		//TODO: update to show mastodon account once fully implemented upstream
		headerSubtitle.setText(getContext().getString(R.string.article_by_author, TextUtils.isEmpty(trendingLink.authorName)? trendingLink.providerName : trendingLink.authorName));
		openLinkButton.setVisibility(View.VISIBLE);
	}

	private void openLink() {
		UiUtils.launchWebBrowser(getActivity(), trendingLink.url);
	}
}
