package net.seqular.network.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.seqular.network.R;
import net.seqular.network.api.requests.tags.GetFollowedTags;
import net.seqular.network.api.requests.tags.SetTagFollowed;
import net.seqular.network.fragments.settings.BaseSettingsFragment;
import net.seqular.network.model.Hashtag;
import net.seqular.network.model.HeaderPaginationList;
import net.seqular.network.model.viewmodel.ListItemWithOptionsMenu;
import net.seqular.network.ui.M3AlertDialogBuilder;
import net.seqular.network.ui.utils.UiUtils;

import java.util.stream.Collectors;

import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;

public class ManageFollowedHashtagsFragment extends BaseSettingsFragment<Hashtag> implements ListItemWithOptionsMenu.OptionsMenuListener<Hashtag>{
	private String maxID;

	public ManageFollowedHashtagsFragment(){
		super(100);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(R.string.manage_hashtags);
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetFollowedTags(offset>0 ? maxID : null, count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(HeaderPaginationList<Hashtag> result){
						maxID=null;
						if(result.nextPageUri!=null)
							maxID=result.nextPageUri.getQueryParameter("max_id");
						onDataLoaded(result.stream().map(t->{
							int posts=t.getWeekPosts();
							return new ListItemWithOptionsMenu<>(t.name, getResources().getQuantityString(R.plurals.x_posts_recently, posts, posts), ManageFollowedHashtagsFragment.this,
									R.drawable.ic_fluent_tag_24_regular, ManageFollowedHashtagsFragment.this::onItemClick, t, false);
						}).collect(Collectors.toList()), maxID!=null);
					}
				})
				.exec(accountID);
	}

	@Override
	public void onConfigureListItemOptionsMenu(ListItemWithOptionsMenu<Hashtag> item, Menu menu){
		menu.clear();
		menu.add(getString(R.string.unfollow_user, "#"+item.parentObject.name));
	}

	@Override
	public void onListItemOptionSelected(ListItemWithOptionsMenu<Hashtag> item, MenuItem menuItem){
		new M3AlertDialogBuilder(getActivity())
				.setTitle(getString(R.string.unfollow_confirmation, "#"+item.parentObject.name))
				.setPositiveButton(R.string.unfollow, (dlg, which)->doUnfollow(item))
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onItemClick(ListItemWithOptionsMenu<Hashtag> item){
		UiUtils.openHashtagTimeline(getActivity(), accountID, item.parentObject);
	}

	private void doUnfollow(ListItemWithOptionsMenu<Hashtag> item){
		new SetTagFollowed(item.parentObject.name, false)
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(Hashtag result){
						int index=data.indexOf(item);
						if(index==-1)
							return;
						data.remove(index);
						list.getAdapter().notifyItemRemoved(index);
					}

					@Override
					public void onError(ErrorResponse error){
						error.showToast(getActivity());
					}
				})
				.wrapProgress(getActivity(), R.string.loading, true)
				.exec(accountID);
	}
}
