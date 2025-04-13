package net.seqular.network.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.assist.AssistContent;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import net.seqular.network.E;
import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.R;
import net.seqular.network.api.requests.accounts.GetFollowRequests;
import net.seqular.network.api.requests.markers.SaveMarkers;
import net.seqular.network.api.requests.notifications.PleromaMarkNotificationsRead;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.events.FollowRequestHandledEvent;
import net.seqular.network.model.Account;
import net.seqular.network.model.HeaderPaginationList;
import net.seqular.network.model.PushSubscription;
import net.seqular.network.ui.M3AlertDialogBuilder;
import net.seqular.network.ui.SimpleViewHolder;
import net.seqular.network.ui.tabs.TabLayout;
import net.seqular.network.ui.tabs.TabLayoutMediator;
import net.seqular.network.ui.utils.UiUtils;
import net.seqular.network.utils.ElevationOnScrollListener;
import net.seqular.network.utils.ObjectIdComparator;
import net.seqular.network.utils.ProvidesAssistContent;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.FragmentRootLinearLayout;

public class NotificationsFragment extends MastodonToolbarFragment implements ScrollableToTop, ProvidesAssistContent, HasElevationOnScrollListener, HasAccountID {

	TabLayout tabLayout;
	private ViewPager2 pager;
	private FrameLayout[] tabViews;
	private View tabsDivider;
	private TabLayoutMediator tabLayoutMediator;
	String unreadMarker, realUnreadMarker;
	private MenuItem markAllReadItem, filterItem;
	private NotificationsListFragment allNotificationsFragment, mentionsFragment;
	private ElevationOnScrollListener elevationOnScrollListener;

	private String accountID;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
			setRetainInstance(true);

		accountID=getArguments().getString("account");
		E.register(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		E.unregister(this);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setHasOptionsMenu(true);
		setTitle(R.string.notifications);
	}

	@Override
	public void onShown() {
		super.onShown();
		unreadMarker=realUnreadMarker=AccountSessionManager.get(accountID).getLastKnownNotificationsMarker();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.notifications, menu);
		menu.findItem(R.id.clear_notifications).setVisible(GlobalUserPreferences.enableDeleteNotifications);
		filterItem=menu.findItem(R.id.filter_notifications).setVisible(true);
		markAllReadItem=menu.findItem(R.id.mark_all_read);
		updateMarkAllReadButton();
		UiUtils.enableOptionsMenuIcons(getActivity(), menu, R.id.follow_requests, R.id.mark_all_read, R.id.filter_notifications);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.follow_requests) {
			Bundle args=new Bundle();
			args.putString("account", accountID);
			Nav.go(getActivity(), FollowRequestsListFragment.class, args);
			return true;
		} else if (item.getItemId() == R.id.clear_notifications) {
			UiUtils.confirmDeleteNotification(getActivity(), accountID, null, ()->{
				for (int i = 0; i < tabViews.length; i++) {
					getFragmentForPage(i).reload();
				}
			});
			return true;
		} else if (item.getItemId() == R.id.mark_all_read) {
			markAsRead();
			if (getCurrentFragment() instanceof NotificationsListFragment nlf) {
				nlf.resetUnreadBackground();
			}
			return true;
		} else if (item.getItemId() == R.id.filter_notifications) {
			Context ctx = getToolbarContext();
			String[] listItems = {
					ctx.getString(R.string.notification_type_mentions_and_replies),
					ctx.getString(R.string.notification_type_reblog),
					ctx.getString(R.string.notification_type_favorite),
					ctx.getString(R.string.notification_type_follow),
					ctx.getString(R.string.notification_type_poll),
					ctx.getString(R.string.sk_notification_type_update),
					ctx.getString(R.string.sk_notification_type_posts)
			};

			boolean[] checkedItems = {
					getLocalPrefs().notificationFilters.mention,
					getLocalPrefs().notificationFilters.reblog,
					getLocalPrefs().notificationFilters.favourite,
					getLocalPrefs().notificationFilters.follow,
					getLocalPrefs().notificationFilters.poll,
					getLocalPrefs().notificationFilters.update,
					getLocalPrefs().notificationFilters.status,
			};

			M3AlertDialogBuilder dialogBuilder = new M3AlertDialogBuilder(ctx);
			dialogBuilder.setTitle(R.string.sk_settings_filters);
			dialogBuilder.setMultiChoiceItems(listItems, checkedItems, (dialog, which, isChecked) ->checkedItems[which] = isChecked);

			dialogBuilder.setPositiveButton(R.string.save, (d, which) -> {
				saveFilters(checkedItems);
				this.allNotificationsFragment.reload();
			}).setNeutralButton(R.string.mo_notification_filter_reset, (d, which) -> {
				Arrays.fill(checkedItems, true);
				saveFilters(checkedItems);
				this.allNotificationsFragment.reload();
			}).setNegativeButton(R.string.cancel, (d, which) -> {});

			dialogBuilder.create().show();

			return true;
		}
		return false;
	}

	private void saveFilters(boolean[] checkedItems) {
		PushSubscription.Alerts filter = getLocalPrefs().notificationFilters;
		filter.mention = checkedItems[0];
		filter.reblog = checkedItems[1];
		filter.favourite = checkedItems[2];
		filter.follow = checkedItems[3];
		filter.poll = checkedItems[4];
		filter.update = checkedItems[5];
		filter.status = checkedItems[6];
		getLocalPrefs().save();
	}

	void markAsRead(){
		if(allNotificationsFragment.getData().isEmpty()) return;
		String id=allNotificationsFragment.getData().get(0).id;
		if(ObjectIdComparator.INSTANCE.compare(id, realUnreadMarker)>0){
			new SaveMarkers(null, id).exec(accountID);
			if (allNotificationsFragment.isInstanceAkkoma()) {
				new PleromaMarkNotificationsRead(id).exec(accountID);
			}
			AccountSessionManager.get(accountID).setNotificationsMarker(id, true);
			realUnreadMarker=id;
			updateMarkAllReadButton();
		}
	}

	public void updateMarkAllReadButton(){
		markAllReadItem.setVisible(!allNotificationsFragment.getData().isEmpty() && realUnreadMarker!=null && !realUnreadMarker.equals(allNotificationsFragment.getData().get(0).id));
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		LinearLayout view=(LinearLayout) inflater.inflate(R.layout.fragment_notifications, container, false);

		tabLayout=view.findViewById(R.id.tabbar);
		tabsDivider=view.findViewById(R.id.tabs_divider);
		pager=view.findViewById(R.id.pager);
		UiUtils.reduceSwipeSensitivity(pager);

		tabViews=new FrameLayout[2];
		for(int i=0;i<tabViews.length;i++){
			FrameLayout tabView=new FrameLayout(getActivity());
			tabView.setId(switch(i){
				case 0 -> R.id.notifications_all;
				case 1 -> R.id.notifications_mentions;
				default -> throw new IllegalStateException("Unexpected value: "+i);
			});
			tabView.setVisibility(View.GONE);
			view.addView(tabView); // needed so the fragment manager will have somewhere to restore the tab fragment
			tabViews[i]=tabView;
		}

		tabLayout.setTabTextSize(V.dp(16));
		tabLayout.setTabTextColors(UiUtils.getThemeColor(getActivity(), R.attr.colorM3OnSurfaceVariant), UiUtils.getThemeColor(getActivity(), R.attr.colorM3Primary));
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
				scrollToTop();
			}
		});

		pager.setOffscreenPageLimit(4);
		pager.setUserInputEnabled(!GlobalUserPreferences.disableSwipe);
		pager.setAdapter(new DiscoverPagerAdapter());
		pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
			@Override
			public void onPageSelected(int position){
				if (elevationOnScrollListener != null && getCurrentFragment() instanceof IsOnTop f)
					elevationOnScrollListener.handleScroll(getContext(), f.isOnTop());
				filterItem.setVisible(position==0);
				if(position==0)
					return;
				Fragment _page=getFragmentForPage(position);
				if(_page instanceof BaseRecyclerFragment<?> page){
					if(!page.loaded && !page.isDataLoading())
						page.loadData();
				}
			}
		});

		if(allNotificationsFragment==null){
			Bundle args=new Bundle();
			args.putString("account", accountID);
			args.putBoolean("__is_tab", true);

			allNotificationsFragment=new NotificationsListFragment();
			allNotificationsFragment.setArguments(args);

			args=new Bundle(args);
			args.putBoolean("onlyMentions", true);
			mentionsFragment=new NotificationsListFragment();
			mentionsFragment.setArguments(args);

			getChildFragmentManager().beginTransaction()
					.add(R.id.notifications_all, allNotificationsFragment)
					.add(R.id.notifications_mentions, mentionsFragment)
					.commit();
		}

		tabLayoutMediator=new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy(){
			@Override
			public void onConfigureTab(@NonNull TabLayout.Tab tab, int position){
				tab.setText(switch(position){
					case 0 -> R.string.all_notifications;
					case 1 -> R.string.mentions;
					default -> throw new IllegalStateException("Unexpected value: "+position);
				});
			}
		});
		tabLayoutMediator.attach();

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		elevationOnScrollListener = new ElevationOnScrollListener((FragmentRootLinearLayout) view, getToolbar(), tabLayout);
		elevationOnScrollListener.setDivider(tabsDivider);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (elevationOnScrollListener == null) return;
		elevationOnScrollListener.setViews(getToolbar(), tabLayout);
		if (getCurrentFragment() instanceof IsOnTop f) {
			elevationOnScrollListener.handleScroll(getContext(), f.isOnTop());
		}
	}

	@Override
	public ElevationOnScrollListener getElevationOnScrollListener() {
		return elevationOnScrollListener;
	}

	public void refreshFollowRequestsBadge() {
		new GetFollowRequests(null, 1).setCallback(new Callback<>() {
			@Override
			public void onSuccess(HeaderPaginationList<Account> accounts) {
				if(getActivity()==null) return;
				getToolbar().getMenu().findItem(R.id.follow_requests).setVisible(!accounts.isEmpty());
			}

			@Override
			public void onError(ErrorResponse errorResponse) {}
		}).exec(accountID);
	}

	@Subscribe
	public void onFollowRequestHandled(FollowRequestHandledEvent ev) {
		refreshFollowRequestsBadge();
	}

	@Override
	public void scrollToTop(){
		if (getFragmentForPage(pager.getCurrentItem()).isOnTop() && GlobalUserPreferences.doubleTapToSwipe) {
			int nextPage = (pager.getCurrentItem() + 1) % tabViews.length;
			pager.setCurrentItem(nextPage, true);
			return;
		}
		getFragmentForPage(pager.getCurrentItem()).scrollToTop();
	}


	public void loadData(){
		refreshFollowRequestsBadge();
		if(allNotificationsFragment!=null && !allNotificationsFragment.loaded && !allNotificationsFragment.dataLoading)
			allNotificationsFragment.loadData();
	}

	@Override
	protected void updateToolbar(){
		super.updateToolbar();
		getToolbar().setOutlineProvider(null);
		getToolbar().setOnClickListener(v->scrollToTop());
	}

	private NotificationsListFragment getFragmentForPage(int page){
		return switch(page){
			case 0 -> allNotificationsFragment;
			case 1 -> mentionsFragment;
			default -> throw new IllegalStateException("Unexpected value: "+page);
		};
	}

	public Fragment getCurrentFragment() {
		return getFragmentForPage(pager.getCurrentItem());
	}

	@Override
	public void onProvideAssistContent(AssistContent assistContent) {
		callFragmentToProvideAssistContent(getFragmentForPage(pager.getCurrentItem()), assistContent);
	}

	@Override
	public String getAccountID(){
		return accountID;
	}

	private class DiscoverPagerAdapter extends RecyclerView.Adapter<SimpleViewHolder>{
		@NonNull
		@Override
		public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			FrameLayout view=tabViews[viewType];
			if (view.getParent() != null) ((ViewGroup)view.getParent()).removeView(view);
			view.setVisibility(View.VISIBLE);
			view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			return new SimpleViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position){}

		@Override
		public int getItemCount(){
			return 2;
		}

		@Override
		public int getItemViewType(int position){
			return position;
		}
	}
}
