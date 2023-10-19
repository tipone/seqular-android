package org.joinmastodon.android.fragments.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.joinmastodon.android.BuildConfig;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.MastodonAPIController;
import org.joinmastodon.android.api.session.AccountSession;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.fragments.HasAccountID;
import org.joinmastodon.android.model.viewmodel.ListItem;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.imageloader.ImageCache;
import me.grishka.appkit.utils.MergeRecyclerAdapter;
import me.grishka.appkit.utils.SingleViewRecyclerAdapter;
import me.grishka.appkit.utils.V;

public class SettingsAboutAppFragment extends BaseSettingsFragment<Void> implements HasAccountID{
	private ListItem<Void> mediaCacheItem;
	private AccountSession session;
	private boolean timelineCacheCleared=false;

	// MOSHIDON
	private ListItem<Void> clearRecentEmojisItem;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.about_app, getString(R.string.mo_app_name)));
		session=AccountSessionManager.get(accountID);
		onDataLoaded(List.of(
				new ListItem<>(R.string.sk_settings_donate, 0, R.drawable.ic_fluent_heart_24_regular, i->UiUtils.launchWebBrowser(getActivity(), getString(R.string.mo_donate_url))),
				new ListItem<>(R.string.mo_settings_contribute, 0, R.drawable.ic_fluent_open_24_regular, i->UiUtils.launchWebBrowser(getActivity(), getString(R.string.mo_repo_url))),
				new ListItem<>(R.string.settings_tos, 0, R.drawable.ic_fluent_open_24_regular, i->UiUtils.launchWebBrowser(getActivity(), "https://"+session.domain+"/terms")),
				new ListItem<>(R.string.settings_privacy_policy, 0, R.drawable.ic_fluent_open_24_regular, i->UiUtils.launchWebBrowser(getActivity(), getString(R.string.privacy_policy_url)), 0, true),
				clearRecentEmojisItem=new ListItem<>(R.string.mo_clear_recent_emoji, 0, this::onClearRecentEmojisClick),
				mediaCacheItem=new ListItem<>(R.string.settings_clear_cache, 0, this::onClearMediaCacheClick),
				new ListItem<>(getString(R.string.sk_settings_clear_timeline_cache), session.domain, this::onClearTimelineCacheClick)
		));

		updateMediaCacheItem();
	}

	@Override
	protected void onHidden(){
		super.onHidden();
		if(timelineCacheCleared) getActivity().recreate();
	}

	@Override
	protected void doLoadData(int offset, int count){}

	@Override
	protected RecyclerView.Adapter<?> getAdapter(){
		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		adapter.addAdapter(super.getAdapter());

		TextView versionInfo=new TextView(getActivity());
		versionInfo.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, V.dp(32)));
		versionInfo.setTextAppearance(R.style.m3_label_medium);
		versionInfo.setTextColor(UiUtils.getThemeColor(getActivity(), R.attr.colorM3Outline));
		versionInfo.setGravity(Gravity.CENTER);
		versionInfo.setText(getString(R.string.mo_settings_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
		adapter.addAdapter(new SingleViewRecyclerAdapter(versionInfo));

		return adapter;
	}

	private void onClearMediaCacheClick(ListItem<?> item){
		MastodonAPIController.runInBackground(()->{
			Activity activity=getActivity();
			ImageCache.getInstance(getActivity()).clear();
			activity.runOnUiThread(()->{
				Toast.makeText(activity, R.string.media_cache_cleared, Toast.LENGTH_SHORT).show();
				updateMediaCacheItem();
			});
		});
	}

	private void onClearTimelineCacheClick(ListItem<?> item){
		session.getCacheController().putHomeTimeline(List.of(), true);
		Toast.makeText(getContext(), R.string.sk_timeline_cache_cleared, Toast.LENGTH_SHORT).show();
		timelineCacheCleared=true;
	}

	private void onClearRecentEmojisClick(ListItem<?> item){
		getLocalPrefs().recentCustomEmoji=new ArrayList<>();
		getLocalPrefs().save();
		Toast.makeText(getContext(), R.string.mo_recent_emoji_cleared, Toast.LENGTH_SHORT).show();
	}

	private void updateMediaCacheItem(){
		long size=ImageCache.getInstance(getActivity()).getDiskCache().size();
		mediaCacheItem.subtitle=UiUtils.formatFileSize(getActivity(), size, false);
		mediaCacheItem.isEnabled=size>0;
		rebindItem(mediaCacheItem);
	}

	@Override
	public String getAccountID(){
		return accountID;
	}
}
