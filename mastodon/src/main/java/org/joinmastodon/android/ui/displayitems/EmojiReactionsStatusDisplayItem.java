package org.joinmastodon.android.ui.displayitems;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.joinmastodon.android.E;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.MastodonAPIRequest;
import org.joinmastodon.android.api.requests.announcements.AddAnnouncementReaction;
import org.joinmastodon.android.api.requests.announcements.DeleteAnnouncementReaction;
import org.joinmastodon.android.api.requests.statuses.AddStatusReaction;
import org.joinmastodon.android.api.requests.statuses.DeleteStatusReaction;
import org.joinmastodon.android.api.requests.statuses.PleromaAddStatusReaction;
import org.joinmastodon.android.api.requests.statuses.PleromaDeleteStatusReaction;
import org.joinmastodon.android.api.session.AccountSession;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.events.StatusCountersUpdatedEvent;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.fragments.account_list.StatusEmojiReactionsListFragment;
import org.joinmastodon.android.model.Account;
import org.joinmastodon.android.model.Announcement;
import org.joinmastodon.android.model.Emoji;
import org.joinmastodon.android.model.EmojiReaction;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.CustomEmojiPopupKeyboard;
import org.joinmastodon.android.ui.utils.TextDrawable;
import org.joinmastodon.android.ui.utils.UiUtils;
import org.joinmastodon.android.ui.views.ProgressBarButton;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.ListImageLoaderWrapper;
import me.grishka.appkit.imageloader.RecyclerViewDelegate;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.UsableRecyclerView;

public class EmojiReactionsStatusDisplayItem extends StatusDisplayItem {
	public final Status status;
	private final Drawable placeholder;
	private final boolean hideAdd, forAnnouncement;
	private final String accountID;
	private boolean hidden;

    public EmojiReactionsStatusDisplayItem(String parentID, BaseStatusListFragment<?> parentFragment, Status status, String accountID, boolean hideAdd, boolean forAnnouncement) {
		super(parentID, parentFragment);
		this.status=status;
		this.hideAdd=hideAdd;
		this.forAnnouncement=forAnnouncement;
		this.accountID=accountID;
		placeholder=parentFragment.getContext().getDrawable(R.drawable.image_placeholder).mutate();
		placeholder.setBounds(0, 0, V.sp(24), V.sp(24));
		updateHidden();
    }

	@Override
	public int getImageCount(){
		return (int) status.reactions.stream().filter(r->r.url != null).count();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return status.reactions.get(index).request;
	}

    @Override
    public Type getType(){
        return Type.EMOJI_REACTIONS;
    }

	public boolean isHidden(){
		return hidden;
	}

	private void updateHidden(){
		hidden=status.reactions.isEmpty() && hideAdd;
	}

	// borrowed from ProfileFragment
	private void setActionProgressVisible(Holder.EmojiReactionViewHolder vh, boolean visible){
		if(vh==null) return;
		vh.progress.setVisibility(visible ? View.VISIBLE : View.GONE);
		if(visible)
			vh.progress.setIndeterminateTintList(vh.btn.getTextColors());
		vh.btn.setClickable(!visible);
	}

	private MastodonAPIRequest<?> createRequest(String name, int count, boolean delete, Holder.EmojiReactionViewHolder vh, Runnable cb){
		setActionProgressVisible(vh, true);
		boolean ak=parentFragment.isInstanceAkkoma();
		boolean keepSpinning=delete && count == 1;
		if(forAnnouncement){
			MastodonAPIRequest<Object> req=delete
					? new DeleteAnnouncementReaction(status.id, name)
					: new AddAnnouncementReaction(status.id, name);
			return req.setCallback(new Callback<>(){
				@Override
				public void onSuccess(Object result){
					if(!keepSpinning) setActionProgressVisible(vh, false);
					cb.run();
				}
				@Override
				public void onError(ErrorResponse error){
					setActionProgressVisible(vh, false);
					error.showToast(parentFragment.getContext());
				}
			});
		}else{
			MastodonAPIRequest<Status> req=delete
					? (ak ? new PleromaDeleteStatusReaction(status.id, name) : new DeleteStatusReaction(status.id, name))
					: (ak ? new PleromaAddStatusReaction(status.id, name) : new AddStatusReaction(status.id, name));
			return req.setCallback(new Callback<>(){
				@Override
				public void onSuccess(Status result){
					if(!keepSpinning) setActionProgressVisible(vh, false);
					cb.run();
				}
				@Override
				public void onError(ErrorResponse error){
					setActionProgressVisible(vh, false);
					error.showToast(parentFragment.getContext());
				}
			});
		}
	}

	public static class Holder extends StatusDisplayItem.Holder<EmojiReactionsStatusDisplayItem> implements ImageLoaderViewHolder, CustomEmojiPopupKeyboard.Listener {
		private final UsableRecyclerView list;
		private final LinearLayout root, line;
		private CustomEmojiPopupKeyboard emojiKeyboard;
		private final View space;
		private final ImageButton addButton;
		private final EmojiReactionsAdapter adapter;
		private final ListImageLoaderWrapper imgLoader;

		public Holder(Activity activity, ViewGroup parent) {
			super(activity, R.layout.display_item_emoji_reactions, parent);
			root=(LinearLayout) itemView;
			line=findViewById(R.id.line);
			list=findViewById(R.id.list);
			imgLoader=new ListImageLoaderWrapper(activity, list, new RecyclerViewDelegate(list), null);
			list.setAdapter(adapter=new EmojiReactionsAdapter(this, imgLoader));
			addButton=findViewById(R.id.add_btn);
			addButton.setOnClickListener(this::onReactClick);
			space=findViewById(R.id.space);
			list.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        }

        @Override
        public void onBind(EmojiReactionsStatusDisplayItem item) {
			if(emojiKeyboard != null) root.removeView(emojiKeyboard.getView());
			AccountSession session=item.parentFragment.getSession();
			item.status.reactions.forEach(r->
					r.request=r.url != null ? new UrlImageLoaderRequest(r.url, V.sp(24), V.sp(24)) : null);
			emojiKeyboard=new CustomEmojiPopupKeyboard(
					(Activity) item.parentFragment.getContext(),
					AccountSessionManager.getInstance().getCustomEmojis(session.domain),
					session.domain, true);
			emojiKeyboard.setListener(this);
			space.setVisibility(View.GONE);
			root.addView(emojiKeyboard.getView());
			item.updateHidden();
			root.setVisibility(item.hidden ? View.GONE : View.VISIBLE);
			line.setVisibility(item.hidden ? View.GONE : View.VISIBLE);
			line.setPadding(
					list.getPaddingLeft(),
					item.hidden ? 0 : V.dp(8),
					list.getPaddingRight(),
					item.forAnnouncement ? V.dp(8) : 0
			);
			imgLoader.updateImages();
			adapter.notifyDataSetChanged();
        }

		private void hideEmojiKeyboard(){
			space.setVisibility(View.GONE);
			addButton.setSelected(false);
			if(emojiKeyboard.isVisible()) emojiKeyboard.toggleKeyboardPopup(null);
		}

		@Override
		public void onEmojiSelected(Emoji emoji) {
			addEmojiReaction(emoji.shortcode, emoji);
			hideEmojiKeyboard();
		}

		@Override
		public void onEmojiSelected(String emoji){
			addEmojiReaction(emoji, null);
			hideEmojiKeyboard();
		}

		private void addEmojiReaction(String emoji, Emoji info) {
			if(item.status.reactions.stream().filter(r->r.name.equals(emoji) && r.me).findAny().isPresent()) return;

			Account me=AccountSessionManager.get(item.accountID).self;
			EmojiReaction existing=null;
			for(int i=0; i<item.status.reactions.size(); i++){
				EmojiReaction r=item.status.reactions.get(i);
				if(r.name.equals(emoji)){
					existing=r;
					r.add(me);
					adapter.notifyItemChanged(i);
					break;
				}
			}
			if(existing==null){
				item.status.reactions.add(0, info!=null ? EmojiReaction.of(info, me) : EmojiReaction.of(emoji, me));
				adapter.notifyItemRangeInserted(0, 1);
			}
			E.post(new StatusCountersUpdatedEvent(item.status, adapter.parentHolder));
			item.createRequest(emoji, existing==null ? 1 : existing.count, false, null, ()->{}).exec(item.accountID);
		}

		@Override
		public void onBackspace() {}

		private void onReactClick(View v){
			emojiKeyboard.toggleKeyboardPopup(null);
			v.setSelected(emojiKeyboard.isVisible());
			space.setVisibility(emojiKeyboard.isVisible() ? View.VISIBLE : View.GONE);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			int[] locationOnScreen = new int[2];
			((Activity) v.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			v.getLocationOnScreen(locationOnScreen);
			double fromScreenTop = (double) locationOnScreen[1] / displayMetrics.heightPixels;
			if (fromScreenTop > 0.75) {
				item.parentFragment.scrollBy(0, (int) (displayMetrics.heightPixels * 0.3));
			}
		}

		@Override
		public void setImage(int index, Drawable image){
			View child=list.getChildAt(index);
			if(child==null) return;
			((EmojiReactionViewHolder) list.getChildViewHolder(child)).setImage(index, image);
		}

		@Override
		public void clearImage(int index){
			if(item.status.reactions.get(index).url==null) return;
			setImage(index, item.placeholder);
		}

		private class EmojiReactionsAdapter extends UsableRecyclerView.Adapter<EmojiReactionViewHolder> implements ImageLoaderRecyclerAdapter{
			ListImageLoaderWrapper imgLoader;
			Holder parentHolder;

			public EmojiReactionsAdapter(Holder parentHolder, ListImageLoaderWrapper imgLoader){
				super(imgLoader);
				this.parentHolder=parentHolder;
				this.imgLoader=imgLoader;
			}

			@NonNull
			@Override
			public EmojiReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
				return new EmojiReactionViewHolder(parent.getContext(), list);
			}

			@Override
			public void onBindViewHolder(EmojiReactionViewHolder holder, int position){
				holder.bind(Pair.create(item, item.status.reactions.get(position)));
				super.onBindViewHolder(holder, position);
			}

			@Override
			public int getItemCount(){
				return item.status.reactions.size();
			}

			@Override
			public int getImageCountForItem(int position){
				return item.status.reactions.get(position).url == null ? 0 : 1;
			}

			@Override
			public ImageLoaderRequest getImageRequest(int position, int image){
				return item.status.reactions.get(position).request;
			}
		}

		private static class EmojiReactionViewHolder extends BindableViewHolder<Pair<EmojiReactionsStatusDisplayItem, EmojiReaction>> implements ImageLoaderViewHolder{
			private final ProgressBarButton btn;
			private final ProgressBar progress;

			public EmojiReactionViewHolder(Context context, RecyclerView list){
				super(context, R.layout.item_emoji_reaction, list);
				btn=findViewById(R.id.btn);
				progress=findViewById(R.id.progress);
				itemView.setClickable(true);
			}

			@Override
			public void setImage(int index, Drawable drawable){
				drawable.setBounds(0, 0, V.sp(24), V.sp(24));
				btn.setCompoundDrawablesRelative(drawable, null, null, null);
				if(drawable instanceof Animatable) ((Animatable) drawable).start();
			}

			@Override
			public void clearImage(int index){
				setImage(index, item.first.placeholder);
			}

			@Override
			public void onBind(Pair<EmojiReactionsStatusDisplayItem, EmojiReaction> item){
				item.first.setActionProgressVisible(this, false);
				EmojiReactionsStatusDisplayItem parent=item.first;
				EmojiReaction reaction=item.second;
				btn.setText(UiUtils.abbreviateNumber(reaction.count));
				btn.setContentDescription(reaction.name);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) btn.setTooltipText(reaction.name);
				if(reaction.url==null){
					Paint p=new Paint();
					p.setTextSize(V.sp(18));
					TextDrawable drawable=new TextDrawable(p, reaction.name);
					btn.setCompoundDrawablesRelative(drawable, null, null, null);
				}else{
					btn.setCompoundDrawablesRelative(item.first.placeholder, null, null, null);
				}
				btn.setSelected(reaction.me);
				btn.setOnClickListener(e->{
					boolean deleting=reaction.me;
					parent.createRequest(reaction.name, reaction.count, deleting, this, ()->{
						EmojiReactionsAdapter adapter = (EmojiReactionsAdapter) getBindingAdapter();
						for(int i=0; i<parent.status.reactions.size(); i++){
							EmojiReaction r=parent.status.reactions.get(i);
							if(!r.name.equals(reaction.name)) continue;
							if(deleting && r.count==1) {
								parent.status.reactions.remove(i);
								adapter.notifyItemRemoved(i);
								break;
							}
							r.me=!deleting;
							if(deleting) r.count--;
							else r.count++;
							adapter.notifyItemChanged(i);
							break;
						}

						E.post(new StatusCountersUpdatedEvent(parent.status, adapter.parentHolder));
						adapter.parentHolder.imgLoader.updateImages();
					}).exec(parent.parentFragment.getAccountID());
				});

				if (parent.parentFragment.isInstanceAkkoma()) {
					// glitch-soc doesn't have this, afaik
					btn.setOnLongClickListener(e->{
						EmojiReaction emojiReaction=parent.status.reactions.get(getAbsoluteAdapterPosition());
						Bundle args=new Bundle();
						args.putString("account", parent.parentFragment.getAccountID());
						args.putString("statusID", parent.status.id);
						int atSymbolIndex = emojiReaction.name.indexOf("@");
						args.putString("emoji", atSymbolIndex != -1 ? emojiReaction.name.substring(0, atSymbolIndex) : emojiReaction.name);
						args.putString("url", emojiReaction.url);
						args.putInt("count", emojiReaction.count);
						Nav.go(parent.parentFragment.getActivity(), StatusEmojiReactionsListFragment.class, args);
						return true;
					});
				}
			}
		}
	}
}
