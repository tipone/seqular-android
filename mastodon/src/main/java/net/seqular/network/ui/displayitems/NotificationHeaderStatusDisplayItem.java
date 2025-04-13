package net.seqular.network.ui.displayitems;

import static net.seqular.network.MastodonApp.context;
import static net.seqular.network.ui.utils.UiUtils.generateFormattedString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.R;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.fragments.BaseStatusListFragment;
import net.seqular.network.fragments.NotificationsListFragment;
import net.seqular.network.fragments.ProfileFragment;
import net.seqular.network.model.Emoji;
import net.seqular.network.model.Notification;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.text.HtmlParser;
import net.seqular.network.ui.utils.CustomEmojiHelper;
import net.seqular.network.ui.utils.UiUtils;
import org.parceler.Parcels;

import java.util.Collections;

import me.grishka.appkit.Nav;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.V;

public class NotificationHeaderStatusDisplayItem extends StatusDisplayItem{
	public final Notification notification;
	private ImageLoaderRequest avaRequest;
	private final String accountID;
	private final CustomEmojiHelper emojiHelper=new CustomEmojiHelper();
	private final CharSequence text;
	private final CharSequence timestamp;

	public NotificationHeaderStatusDisplayItem(String parentID, BaseStatusListFragment parentFragment, Notification notification, String accountID){
		super(parentID, parentFragment);
		this.notification=notification;
		this.accountID=accountID;
		this.timestamp=notification.createdAt==null ? null : UiUtils.formatRelativeTimestamp(context, notification.createdAt);

		if(notification.type==Notification.Type.POLL){
			text=parentFragment.getString(R.string.poll_ended);
		}else{
			AccountSession session = AccountSessionManager.get(accountID);
			avaRequest=new UrlImageLoaderRequest(
					TextUtils.isEmpty(notification.account.avatar) ? session.getDefaultAvatarUrl() :
						GlobalUserPreferences.playGifs ? notification.account.avatar : notification.account.avatarStatic,
					V.dp(50), V.dp(50));
			SpannableStringBuilder parsedName=new SpannableStringBuilder(notification.account.getDisplayName());
			HtmlParser.parseCustomEmoji(parsedName, notification.account.emojis);
			String str = parentFragment.getString(switch(notification.type){
				case FOLLOW -> R.string.user_followed_you;
				case FOLLOW_REQUEST -> R.string.user_sent_follow_request;
				case REBLOG -> R.string.notification_boosted;
				case FAVORITE -> R.string.user_favorited;
				case POLL -> R.string.poll_ended;
				case UPDATE -> R.string.sk_post_edited;
				case SIGN_UP -> R.string.sk_signed_up;
				case REPORT -> R.string.sk_reported;
				case REACTION, PLEROMA_EMOJI_REACTION ->
						!TextUtils.isEmpty(notification.emoji) ? R.string.sk_reacted_with : R.string.sk_reacted;
				default -> throw new IllegalStateException("Unexpected value: "+notification.type);
			});

			if (!TextUtils.isEmpty(notification.emoji)) {
				SpannableStringBuilder emoji = new SpannableStringBuilder(notification.emoji);
				if (!TextUtils.isEmpty(notification.emojiUrl)) {
					HtmlParser.parseCustomEmoji(emoji, Collections.singletonList(new Emoji(
							notification.emoji, notification.emojiUrl, notification.emojiUrl
					)));
				}
				this.text = generateFormattedString(str, parsedName, emoji);
			} else {
				this.text = generateFormattedString(str, parsedName);
			}

			emojiHelper.setText(text);
		}
	}

	@Override
	public Type getType(){
		return Type.NOTIFICATION_HEADER;
	}

	@Override
	public int getImageCount(){
		return 1+emojiHelper.getImageCount();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		if(index>0){
			return emojiHelper.getImageRequest(index-1);
		}
		return avaRequest;
	}

	public static class Holder extends StatusDisplayItem.Holder<NotificationHeaderStatusDisplayItem> implements ImageLoaderViewHolder{
		private final ImageView icon, avatar, deleteNotification;
		private final TextView text, timestamp;

		public Holder(Activity activity, ViewGroup parent){
			super(activity, R.layout.display_item_notification_header, parent);
			icon=findViewById(R.id.icon);
			avatar=findViewById(R.id.avatar);
			text=findViewById(R.id.text);
			timestamp=findViewById(R.id.timestamp);
			deleteNotification=findViewById(R.id.delete_notification);

			avatar.setOutlineProvider(OutlineProviders.roundedRect(8));
			avatar.setClipToOutline(true);
			deleteNotification.setOnClickListener(v->UiUtils.confirmDeleteNotification(activity, item.parentFragment.getAccountID(), item.notification, ()->{
				if (item.parentFragment instanceof NotificationsListFragment fragment) {
					fragment.removeNotification(item.notification);
				}
			}));

			itemView.setOnClickListener(this::onItemClick);
		}

		@Override
		public void setImage(int index, Drawable image){
			if(index==0){
				avatar.setImageDrawable(image);
			}else{
				item.emojiHelper.setImageDrawable(index-1, image);
				text.setText(text.getText());
			}
			if(image instanceof Animatable)
				((Animatable) image).start();
		}

		@Override
		public void clearImage(int index){
			if(index==0)
				avatar.setImageResource(R.drawable.image_placeholder);
			else
				ImageLoaderViewHolder.super.clearImage(index);
		}

		@SuppressLint("ResourceType")
		@Override
		public void onBind(NotificationHeaderStatusDisplayItem item){
			text.setText(item.text);
			timestamp.setText(item.timestamp);
			avatar.setVisibility(item.notification.type==Notification.Type.POLL ? View.GONE : View.VISIBLE);
			icon.setImageResource(switch(item.notification.type){
				case FAVORITE -> GlobalUserPreferences.likeIcon ? R.drawable.ic_fluent_heart_24_filled : R.drawable.ic_fluent_star_24_filled;
				case REBLOG -> R.drawable.ic_fluent_arrow_repeat_all_24_filled;
				case FOLLOW, FOLLOW_REQUEST -> R.drawable.ic_fluent_person_add_24_filled;
				case POLL -> R.drawable.ic_fluent_poll_24_filled;
				case REPORT -> R.drawable.ic_fluent_warning_24_filled;
				case SIGN_UP -> R.drawable.ic_fluent_person_available_24_filled;
				case UPDATE -> R.drawable.ic_fluent_edit_24_filled;
				case REACTION, PLEROMA_EMOJI_REACTION -> R.drawable.ic_fluent_add_24_filled;
				default -> throw new IllegalStateException("Unexpected value: "+item.notification.type);
			});
			icon.setImageTintList(ColorStateList.valueOf(UiUtils.getThemeColor(item.parentFragment.getActivity(), switch(item.notification.type){
				case FAVORITE -> GlobalUserPreferences.likeIcon ? R.attr.colorLike : R.attr.colorFavorite;
				case REBLOG -> R.attr.colorBoost;
				case POLL -> R.attr.colorPoll;
				default -> android.R.attr.colorAccent;
			})));
			deleteNotification.setVisibility(GlobalUserPreferences.enableDeleteNotifications && item.notification != null ? View.VISIBLE : View.GONE);
			itemView.setBackgroundResource(0);
		}

		public void onItemClick(View v) {
			if (item.notification.type == Notification.Type.REPORT) {
				UiUtils.showFragmentForNotification(item.parentFragment.getContext(), item.notification, item.accountID, null);
				return;
			}
			Bundle args=new Bundle();
			args.putString("account", item.accountID);
			args.putParcelable("profileAccount", Parcels.wrap(item.notification.account));
			Nav.go(item.parentFragment.getActivity(), ProfileFragment.class, args);
		}
	}
}
