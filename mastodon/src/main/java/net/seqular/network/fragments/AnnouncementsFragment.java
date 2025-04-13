package net.seqular.network.fragments;

import static java.util.stream.Collectors.toList;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;

import net.seqular.network.R;
import net.seqular.network.api.requests.announcements.GetAnnouncements;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.model.Account;
import net.seqular.network.model.Announcement;
import net.seqular.network.model.Instance;
import net.seqular.network.model.Status;
import net.seqular.network.ui.displayitems.EmojiReactionsStatusDisplayItem;
import net.seqular.network.ui.displayitems.HeaderStatusDisplayItem;
import net.seqular.network.ui.displayitems.StatusDisplayItem;
import net.seqular.network.ui.displayitems.TextStatusDisplayItem;
import net.seqular.network.ui.text.HtmlParser;

import java.util.ArrayList;
import java.util.List;

import me.grishka.appkit.api.SimpleCallback;

public class AnnouncementsFragment extends BaseStatusListFragment<Announcement> {
	private Instance instance;
	private AccountSession session;
	private List<String> unreadIDs = null;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle(R.string.sk_announcements);
		session = AccountSessionManager.getInstance().getAccount(accountID);
		instance = AccountSessionManager.getInstance().getInstanceInfo(session.domain);
		loadData();
	}

	@Override
	protected List<StatusDisplayItem> buildDisplayItems(Announcement a) {
		if(TextUtils.isEmpty(a.content)) return List.of();
		Account instanceUser = new Account();
		instanceUser.id = instanceUser.acct = instanceUser.username = session.domain;
		instanceUser.displayName = instance.title;
		instanceUser.url = "https://"+session.domain+"/about";
		instanceUser.avatar = instanceUser.avatarStatic = instance.thumbnail;
		instanceUser.emojis = List.of();
		Status fakeStatus = a.toStatus(isInstanceIceshrimp());
		TextStatusDisplayItem textItem = new TextStatusDisplayItem(a.id, HtmlParser.parse(a.content, a.emojis, a.mentions, a.tags, accountID), this, fakeStatus, true);
		textItem.textSelectable = true;

		List<StatusDisplayItem> items=new ArrayList<>();
		items.add(HeaderStatusDisplayItem.fromAnnouncement(a, fakeStatus, instanceUser, this, accountID, this::onMarkAsRead));
		items.add(textItem);
		if(!isInstanceAkkoma() && !isInstanceIceshrimp()) items.add(new EmojiReactionsStatusDisplayItem(a.id, this, fakeStatus, accountID, false, true));
		return items;
	}

	public void onMarkAsRead(String id) {
		if (unreadIDs == null) return;
		unreadIDs.remove(id);
		if (unreadIDs.isEmpty()) setResult(true, null);
	}

	@Override
	protected void addAccountToKnown(Announcement s) {}

	@Override
	public void onItemClick(String id) {}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetAnnouncements(true)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Announcement> result){
						if(getActivity()==null) return;

						// get unread items first
						List<Announcement> data = result.stream().filter(a -> !a.read).collect(toList());
						if (data.isEmpty()) setResult(true, null);
						else unreadIDs = data.stream().map(a -> a.id).collect(toList());

						// append read items at the end
						data.addAll(result.stream().filter(a -> a.read).collect(toList()));
						onDataLoaded(data, false);
					}
				})
				.exec(accountID);
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return isInstanceAkkoma() ? base.path("/announcements").build() : null;
	}
}
