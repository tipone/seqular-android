package org.joinmastodon.android.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import org.joinmastodon.android.R;
import org.joinmastodon.android.api.requests.statuses.GetStatusContext;
import org.joinmastodon.android.events.StatusCreatedEvent;
import org.joinmastodon.android.model.Account;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.model.StatusContext;
import org.joinmastodon.android.ui.displayitems.ExtendedFooterStatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.FooterStatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.ReblogOrReplyLineStatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.StatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.TextStatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.WarningFilteredStatusDisplayItem;
import org.joinmastodon.android.ui.text.HtmlParser;
import org.joinmastodon.android.ui.utils.UiUtils;
import org.joinmastodon.android.utils.ProvidesAssistContent;
import org.joinmastodon.android.utils.StatusFilterPredicate;
import org.parceler.Parcels;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import me.grishka.appkit.api.SimpleCallback;

public class ThreadFragment extends StatusListFragment implements ProvidesAssistContent {
	protected Status mainStatus;
	private final HashMap<String, NeighborAncestryInfo> ancestryMap = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mainStatus=Parcels.unwrap(getArguments().getParcelable("status"));
		Account inReplyToAccount=Parcels.unwrap(getArguments().getParcelable("inReplyToAccount"));
		if(inReplyToAccount!=null)
			knownAccounts.put(inReplyToAccount.id, inReplyToAccount);
		data.add(mainStatus);
		onAppendItems(Collections.singletonList(mainStatus));
		setTitle(HtmlParser.parseCustomEmoji(getString(R.string.post_from_user, mainStatus.account.displayName), mainStatus.account.emojis));
	}

	@Override
	protected List<StatusDisplayItem> buildDisplayItems(Status s){
		List<StatusDisplayItem> items=super.buildDisplayItems(s);
		// "what the fuck is a deque"? yes
		// (it's just so the last-added item automatically comes first when looping over it)
		Deque<Integer> deleteTheseItems = new ArrayDeque<>();

		// modifying hidden filtered items if status is displayed as a warning
		List<StatusDisplayItem> itemsToModify =
				(items.get(0) instanceof WarningFilteredStatusDisplayItem warning)
						? warning.filteredItems
						: items;

		for(int i = 0; i < itemsToModify.size(); i++){
			StatusDisplayItem item = itemsToModify.get(i);
			NeighborAncestryInfo ancestryInfo = ancestryMap.get(s.id);
			if (ancestryInfo != null) {
				item.setAncestryInfo(
						ancestryInfo.descendantNeighbor != null,
						ancestryInfo.ancestoringNeighbor != null,
						s.id.equals(mainStatus.id),
						Optional.ofNullable(ancestryInfo.ancestoringNeighbor)
								.map(ancestor -> ancestor.id.equals(mainStatus.id))
								.orElse(false)
				);
			}

			if (item instanceof ReblogOrReplyLineStatusDisplayItem &&
					(!item.isDirectDescendant && item.hasAncestoringNeighbor)) {
				deleteTheseItems.add(i);
			}

			if(s.id.equals(mainStatus.id)){
				if(item instanceof TextStatusDisplayItem text)
					text.textSelectable=true;
				else if(item instanceof FooterStatusDisplayItem footer)
					footer.hideCounts=true;
			}
		}
		for (int deleteThisItem : deleteTheseItems) itemsToModify.remove(deleteThisItem);
		if(s.id.equals(mainStatus.id)) {
			items.add(new ExtendedFooterStatusDisplayItem(s.id, this, s.getContentStatus()));
		}
		return items;
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetStatusContext(mainStatus.id)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(StatusContext result){
						if (getActivity() == null) return;
						if(refreshing){
							data.clear();
							ancestryMap.clear();
							displayItems.clear();
							data.add(mainStatus);
							onAppendItems(Collections.singletonList(mainStatus));
						}

						// TODO: figure out how this code works
						if(isInstanceAkkoma()) sortStatusContext(mainStatus, result);

						result.descendants=filterStatuses(result.descendants);
						result.ancestors=filterStatuses(result.ancestors);

						for (NeighborAncestryInfo i : mapNeighborhoodAncestry(mainStatus, result)) {
							ancestryMap.put(i.status.id, i);
						}

						if(footerProgress!=null)
							footerProgress.setVisibility(View.GONE);
						data.addAll(result.descendants);
						int prevCount=displayItems.size();
						onAppendItems(result.descendants);
						int count=displayItems.size();
						if(!refreshing)
							adapter.notifyItemRangeInserted(prevCount, count-prevCount);
						int prependedCount = prependItems(result.ancestors, !refreshing);
						if (prependedCount > 0 && displayItems.get(prependedCount) instanceof ReblogOrReplyLineStatusDisplayItem) {
							displayItems.remove(prependedCount);
							adapter.notifyItemRemoved(prependedCount);
							count--;
						}
						dataLoaded();
						if(refreshing){
							refreshDone();
							adapter.notifyDataSetChanged();
						}
						list.scrollToPosition(displayItems.size()-count);
					}
				})
				.exec(accountID);
	}

	public static List<NeighborAncestryInfo> mapNeighborhoodAncestry(Status mainStatus, StatusContext context) {
		List<NeighborAncestryInfo> ancestry = new ArrayList<>();

		List<Status> statuses = new ArrayList<>(context.ancestors);
		statuses.add(mainStatus);
		statuses.addAll(context.descendants);

		int count = statuses.size();
		for (int index = 0; index < count; index++) {
			Status current = statuses.get(index);
			ancestry.add(new NeighborAncestryInfo(
					current,
					// descendant neighbor
					Optional
							.ofNullable(count > index + 1 ? statuses.get(index + 1) : null)
							.filter(s -> s.inReplyToId.equals(current.id))
							.orElse(null),
					// ancestoring neighbor
					Optional.ofNullable(index > 0 ? ancestry.get(index - 1) : null)
							.filter(ancestor -> Optional.ofNullable(ancestor.descendantNeighbor)
									.map(ancestorsDescendant -> ancestorsDescendant.id.equals(current.id))
									.orElse(false))
							.map(a -> a.status)
							.orElse(null)
			));
		}

		return ancestry;
	}

	public static void sortStatusContext(Status mainStatus, StatusContext context) {
		List<String> threadIds=new ArrayList<>();
		threadIds.add(mainStatus.id);
		for(Status s:context.descendants){
			if(threadIds.contains(s.inReplyToId)){
				threadIds.add(s.id);
			}
		}
		threadIds.add(mainStatus.inReplyToId);
		for(int i=context.ancestors.size()-1; i >= 0; i--){
			Status s=context.ancestors.get(i);
			if(s.inReplyToId != null && threadIds.contains(s.id)){
				threadIds.add(s.inReplyToId);
			}
		}

		context.ancestors=context.ancestors.stream().filter(s -> threadIds.contains(s.id)).collect(Collectors.toList());
		context.descendants=getDescendantsOrdered(mainStatus.id,
				context.descendants.stream()
						.filter(s -> threadIds.contains(s.id))
						.collect(Collectors.toList()));
	}

	private static List<Status> getDescendantsOrdered(String id, List<Status> statuses){
		List<Status> out=new ArrayList<>();
		for(Status s:getDirectDescendants(id, statuses)){
			out.add(s);
			getDirectDescendants(s.id, statuses).forEach(d ->{
				out.add(d);
				out.addAll(getDescendantsOrdered(d.id, statuses));
			});
		}
		return out;
	}

	private static List<Status> getDirectDescendants(String id, List<Status> statuses){
		return statuses.stream()
				.filter(s -> s.inReplyToId.equals(id))
				.collect(Collectors.toList());
	}

	private List<Status> filterStatuses(List<Status> statuses){
		StatusFilterPredicate statusFilterPredicate=new StatusFilterPredicate(accountID,getFilterContext());
		return statuses.stream()
				.filter(statusFilterPredicate)
				.collect(Collectors.toList());
	}

	@Override
	protected void onShown(){
		super.onShown();
		if(!getArguments().getBoolean("noAutoLoad") && !loaded && !dataLoading){
			dataLoading=true;
			doLoadData();
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		UiUtils.loadCustomEmojiInTextView(toolbarTitleView);
		showContent();
		if(!loaded)
			footerProgress.setVisibility(View.VISIBLE);
	}

	protected void onStatusCreated(StatusCreatedEvent ev){
		if(ev.status.inReplyToId!=null && getStatusByID(ev.status.inReplyToId)!=null){
			onAppendItems(Collections.singletonList(ev.status));
		}
	}

	@Override
	public boolean isItemEnabled(String id){
		return !id.equals(mainStatus.id);
	}

	@Override
	public boolean wantsLightStatusBar(){
		return !UiUtils.isDarkTheme();
	}

	@Override
	public boolean wantsLightNavigationBar(){
		return !UiUtils.isDarkTheme();
	}


	@Override
	protected Filter.FilterContext getFilterContext() {
		return Filter.FilterContext.THREAD;
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return Uri.parse(mainStatus.url);
	}

	protected static class NeighborAncestryInfo {
		protected Status status, descendantNeighbor, ancestoringNeighbor;

		protected NeighborAncestryInfo(@NonNull Status status, Status descendantNeighbor, Status ancestoringNeighbor) {
			this.status = status;
			this.descendantNeighbor = descendantNeighbor;
			this.ancestoringNeighbor = ancestoringNeighbor;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			NeighborAncestryInfo that = (NeighborAncestryInfo) o;
			return status.equals(that.status)
					&& Objects.equals(descendantNeighbor, that.descendantNeighbor)
					&& Objects.equals(ancestoringNeighbor, that.ancestoringNeighbor);
		}

		@Override
		public int hashCode() {
			return Objects.hash(status, descendantNeighbor, ancestoringNeighbor);
		}
	}
}
