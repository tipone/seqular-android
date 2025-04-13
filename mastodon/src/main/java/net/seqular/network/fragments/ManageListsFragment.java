package net.seqular.network.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageButton;

import com.squareup.otto.Subscribe;

import net.seqular.network.E;
import net.seqular.network.R;
import net.seqular.network.api.requests.lists.DeleteList;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.events.ListCreatedEvent;
import net.seqular.network.events.ListDeletedEvent;
import net.seqular.network.events.ListUpdatedEvent;
import net.seqular.network.fragments.settings.BaseSettingsFragment;
import net.seqular.network.model.FollowList;
import net.seqular.network.model.viewmodel.ListItem;
import net.seqular.network.model.viewmodel.ListItemWithOptionsMenu;
import net.seqular.network.ui.M3AlertDialogBuilder;
import net.seqular.network.ui.utils.UiUtils;
import org.parceler.Parcels;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;

public class ManageListsFragment extends BaseSettingsFragment<FollowList> implements ListItemWithOptionsMenu.OptionsMenuListener<FollowList>{
	private ImageButton fab;

	public ManageListsFragment(){
		setListLayoutId(R.layout.recycler_fragment_with_fab);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(R.string.manage_lists);
		loadData();
		setRefreshEnabled(true);
		E.register(this);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		E.unregister(this);
	}

	@Override
	protected void doLoadData(int offset, int count){
		Callback<List<FollowList>> callback=new SimpleCallback<>(this){
			@Override
			public void onSuccess(List<FollowList> result){
				onDataLoaded(result.stream().map(ManageListsFragment.this::makeItem).collect(Collectors.toList()), false);
			}
		};
		if(refreshing){
			AccountSessionManager.get(accountID)
					.getCacheController()
					.reloadLists(callback);
		}else{
			AccountSessionManager.get(accountID)
					.getCacheController()
					.getLists(callback);
		}
	}

	private ListItem<FollowList> makeItem(FollowList l){
		return new ListItemWithOptionsMenu<>(l.title, null, ManageListsFragment.this, R.drawable.ic_list_alt_24px, ManageListsFragment.this::onListClick, l, false);
	}

	private void onListClick(ListItemWithOptionsMenu<FollowList> item){
		Bundle args=new Bundle();
		args.putString("account", accountID);
		args.putParcelable("list", Parcels.wrap(item.parentObject));
		Nav.go(getActivity(), ListTimelineFragment.class, args);
	}

	@Override
	public void onConfigureListItemOptionsMenu(ListItemWithOptionsMenu<FollowList> item, Menu menu){
		menu.add(0, R.id.edit, 0, R.string.edit_list);
		menu.add(0, R.id.delete, 1, R.string.delete_list);
	}

	@Override
	public void onListItemOptionSelected(ListItemWithOptionsMenu<FollowList> item, MenuItem menuItem){
		int id=menuItem.getItemId();
		if(id==R.id.edit){
			Bundle args=new Bundle();
			args.putString("account", accountID);
			args.putParcelable("list", Parcels.wrap(item.parentObject));
			Nav.go(getActivity(), EditListFragment.class, args);
		}else if(id==R.id.delete){
			new M3AlertDialogBuilder(getActivity())
					.setTitle(R.string.delete_list)
					.setMessage(getString(R.string.delete_list_confirm, item.parentObject.title))
					.setPositiveButton(R.string.delete, (dlg, which)->doDeleteList(item.parentObject))
					.setNegativeButton(R.string.cancel, null)
					.show();
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		fab=view.findViewById(R.id.fab);
		fab.setImageResource(R.drawable.ic_fluent_add_24_regular);
		fab.setContentDescription(getString(R.string.create_list));
		fab.setOnClickListener(v->onFabClick());
	}

	@Override
	public void onApplyWindowInsets(WindowInsets insets){
		super.onApplyWindowInsets(insets);
		UiUtils.applyBottomInsetToFAB(fab, insets);
	}

	private void doDeleteList(FollowList list){
		new DeleteList(list.id)
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(Void result){
						for(int i=0;i<data.size();i++){
							if(data.get(i).parentObject==list){
								data.remove(i);
								itemsAdapter.notifyItemRemoved(i);
								AccountSessionManager.get(accountID).getCacheController().deleteList(list.id);
								break;
							}
						}
					}

					@Override
					public void onError(ErrorResponse error){
						Activity activity=getActivity();
						if(activity==null)
							return;
						error.showToast(activity);
					}
				})
				.wrapProgress(getActivity(), R.string.loading, true)
				.exec(accountID);
	}

	@Subscribe
	public void onListUpdated(ListUpdatedEvent ev){
		if(!ev.accountID.equals(accountID))
			return;
		for(ListItem<FollowList> item:data){
			if(item.parentObject.id.equals(ev.list.id)){
				item.parentObject=ev.list;
				item.title=ev.list.title;
				rebindItem(item);
				break;
			}
		}
	}

	@Subscribe
	public void onListDeleted(ListDeletedEvent ev){
		if(!ev.accountID.equals(accountID))
			return;
		int i=0;
		for(ListItem<FollowList> item:data){
			if(item.parentObject.id.equals(ev.listID)){
				data.remove(i);
				itemsAdapter.notifyItemRemoved(i);
				break;
			}
			i++;
		}
	}

	@Subscribe
	public void onListCreated(ListCreatedEvent ev){
		if(!ev.accountID.equals(accountID))
			return;
		ListItem<FollowList> item=makeItem(ev.list);
		data.add(item);
		((List<ListItem<FollowList>>)data).sort(Comparator.comparing(l->l.parentObject.title));
		itemsAdapter.notifyItemInserted(data.indexOf(item));
	}

	private void onFabClick(){
		Bundle args=new Bundle();
		args.putString("account", accountID);
		Nav.go(getActivity(), CreateListFragment.class, args);
	}
}
