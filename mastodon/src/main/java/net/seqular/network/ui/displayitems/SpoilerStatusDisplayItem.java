package net.seqular.network.ui.displayitems;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.seqular.network.R;
import net.seqular.network.fragments.BaseStatusListFragment;
import net.seqular.network.model.Status;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.drawables.SpoilerStripesDrawable;
import net.seqular.network.ui.drawables.TiledDrawable;
import net.seqular.network.ui.text.HtmlParser;
import net.seqular.network.ui.utils.CustomEmojiHelper;

import java.util.ArrayList;

import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;

public class SpoilerStatusDisplayItem extends StatusDisplayItem{
	public final ArrayList<StatusDisplayItem> contentItems=new ArrayList<>();
	private final CharSequence parsedTitle;
	private CharSequence translatedTitle;
	private final CustomEmojiHelper emojiHelper;
	private final Type type;
	private final int attachmentCount;

	public SpoilerStatusDisplayItem(String parentID, BaseStatusListFragment<?> parentFragment, String title, Status statusForContent, Type type){
		super(parentID, parentFragment);
		this.status=statusForContent;
		this.type=type;
		this.attachmentCount=statusForContent.mediaAttachments.size();
		if(TextUtils.isEmpty(title)){
			parsedTitle=HtmlParser.parseCustomEmoji(statusForContent.spoilerText, statusForContent.emojis);
			emojiHelper=new CustomEmojiHelper();
			emojiHelper.setText(parsedTitle);
		}else{
			parsedTitle=title;
			emojiHelper=null;
		}
	}

	@Override
	public int getImageCount(){
		return emojiHelper==null ? 0 : emojiHelper.getImageCount();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return emojiHelper.getImageRequest(index);
	}

	@Override
	public Type getType(){
		return type;
	}

	public static class Holder extends StatusDisplayItem.Holder<SpoilerStatusDisplayItem> implements ImageLoaderViewHolder{
		private final TextView title, action;
		private final View button;
		private final ImageView mediaIcon;

		public Holder(Context context, ViewGroup parent, Type type){
			super(context, R.layout.display_item_spoiler, parent);
			title=findViewById(R.id.spoiler_title);
			action=findViewById(R.id.spoiler_action);
			button=findViewById(R.id.spoiler_button);
			mediaIcon=findViewById(R.id.media_icon);

			button.setOutlineProvider(OutlineProviders.roundedRect(8));
			button.setClipToOutline(true);
			LayerDrawable spoilerBg=(LayerDrawable) button.getBackground().mutate();
			if(type==Type.SPOILER){
				spoilerBg.setDrawableByLayerId(R.id.left_drawable, new SpoilerStripesDrawable(true));
				spoilerBg.setDrawableByLayerId(R.id.right_drawable, new SpoilerStripesDrawable(false));
			}else if(type==Type.FILTER_SPOILER){
				Drawable texture=context.getDrawable(R.drawable.filter_banner_stripe_texture);
				spoilerBg.setDrawableByLayerId(R.id.left_drawable, new TiledDrawable(texture));
				spoilerBg.setDrawableByLayerId(R.id.right_drawable, new TiledDrawable(texture));
			}
			button.setBackground(spoilerBg);
			button.setOnClickListener(v->item.parentFragment.onRevealSpoilerClick(this));
		}

		@Override
		public void onBind(SpoilerStatusDisplayItem item){
			if(item.status.translationState==Status.TranslationState.SHOWN){
				if(item.translatedTitle==null){
					item.translatedTitle=item.status.translation.spoilerText;
				}
				title.setText(item.translatedTitle);
			}else{
				title.setText(item.parsedTitle);
			}
			action.setText(item.status.spoilerRevealed ? R.string.spoiler_hide : R.string.sk_spoiler_show);
			itemView.setPadding(
					itemView.getPaddingLeft(),
					itemView.getPaddingTop(),
					itemView.getPaddingRight(),
					item.inset ? itemView.getPaddingTop() : 0
			);
			mediaIcon.setVisibility(item.attachmentCount > 0 ? View.VISIBLE : View.GONE);
			mediaIcon.setImageResource(item.attachmentCount > 1
					? R.drawable.ic_fluent_image_multiple_24_regular
					: R.drawable.ic_fluent_image_24_regular);
		}

		@Override
		public void setImage(int index, Drawable image){
			item.emojiHelper.setImageDrawable(index, image);
			title.setText(title.getText());
		}

		@Override
		public void clearImage(int index){
			setImage(index, null);
		}
	}
}
