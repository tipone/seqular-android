package net.seqular.network.ui.displayitems;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.seqular.network.R;
import net.seqular.network.fragments.BaseStatusListFragment;
import net.seqular.network.model.Card;
import net.seqular.network.model.Status;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.drawables.BlurhashCrossfadeDrawable;
import net.seqular.network.ui.text.HtmlParser;
import net.seqular.network.ui.utils.UiUtils;

import java.util.Optional;
import java.util.regex.Matcher;

import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;

public class LinkCardStatusDisplayItem extends StatusDisplayItem{
	private final UrlImageLoaderRequest imgRequest;

	public LinkCardStatusDisplayItem(String parentID, BaseStatusListFragment parentFragment, Status status, boolean showImagePreview){
		super(parentID, parentFragment);
		this.status=status;
		if(status.card.image!=null && showImagePreview)
			imgRequest=new UrlImageLoaderRequest(status.card.image, 1000, 1000);
		else
			imgRequest=null;
	}

	@Override
	public Type getType(){
		return status.card.type==Card.Type.VIDEO || (status.card.image!=null && status.card.width>status.card.height) ? Type.CARD_LARGE : Type.CARD_COMPACT;
	}

	@Override
	public int getImageCount(){
		return imgRequest==null ? 0 : 1;
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return imgRequest;
	}

	public static class Holder extends StatusDisplayItem.Holder<LinkCardStatusDisplayItem> implements ImageLoaderViewHolder{
		private final TextView title, description, domain, timestamp;
		private final ImageView photo;
		private BlurhashCrossfadeDrawable crossfadeDrawable=new BlurhashCrossfadeDrawable();
		private boolean didClear;
		private final View inner;
		private final boolean isLarge;

		public Holder(Context context, ViewGroup parent, boolean isLarge){
			super(context, isLarge ? R.layout.display_item_link_card : R.layout.display_item_link_card_compact, parent);
			this.isLarge=isLarge;
			title=findViewById(R.id.title);
			description=findViewById(R.id.description);
			domain=findViewById(R.id.domain);
			timestamp=findViewById(R.id.timestamp);
			photo=findViewById(R.id.photo);
			inner=findViewById(R.id.inner);
			inner.setOnClickListener(this::onClick);
			inner.setOutlineProvider(OutlineProviders.roundedRect(12));
			inner.setClipToOutline(true);
		}

		@SuppressLint("SetTextI18n")
		@Override
		public void onBind(LinkCardStatusDisplayItem item){
			Card card=item.status.card;
			title.setText(card.title);
			if(description!=null){
				description.setText(card.description);
				description.setVisibility(TextUtils.isEmpty(card.description) ? View.GONE : View.VISIBLE);
			}
			String cardDomain=Uri.parse(card.url).getHost();
			if(isLarge && !TextUtils.isEmpty(card.authorName)){
				domain.setText(itemView.getContext().getString(R.string.article_by_author, card.authorName)+" · "+cardDomain);
			}else{
				domain.setText(cardDomain);
			}
			if(card.publishedAt!=null){
				timestamp.setVisibility(View.VISIBLE);
				timestamp.setText(" · "+UiUtils.formatRelativeTimestamp(itemView.getContext(), card.publishedAt));
			}else{
				timestamp.setVisibility(View.GONE);
			}

			photo.setImageDrawable(null);
			if(item.imgRequest!=null){
				photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
				photo.setBackground(null);
				photo.setImageTintList(null);
				crossfadeDrawable.setSize(card.width, card.height);
				if (card.width > 0) {
					// akkoma servers don't provide width and height
					crossfadeDrawable.setSize(card.width, card.height);
				} else {
					crossfadeDrawable.setSize(itemView.getWidth(), itemView.getHeight());
				}
				crossfadeDrawable.setBlurhashDrawable(card.blurhashPlaceholder);
				crossfadeDrawable.setCrossfadeAlpha(0f);
				photo.setImageDrawable(null);
				photo.setImageDrawable(crossfadeDrawable);
				photo.setVisibility(View.VISIBLE);
				didClear=false;
			} else {
				photo.setBackgroundColor(UiUtils.getThemeColor(itemView.getContext(), R.attr.colorM3SurfaceVariant));
				photo.setImageTintList(ColorStateList.valueOf(UiUtils.getThemeColor(itemView.getContext(), R.attr.colorM3Outline)));
				photo.setScaleType(ImageView.ScaleType.CENTER);
				photo.setImageResource(R.drawable.ic_feed_48px);
			}
		}

		@Override
		public void setImage(int index, Drawable drawable){
			crossfadeDrawable.setImageDrawable(drawable);
			if(didClear && item.status.spoilerRevealed)
				crossfadeDrawable.animateAlpha(0f);
			Card card=item.status.card;
			// Make sure the image is not stretched if the server returned wrong dimensions
			if(drawable!=null && (drawable.getIntrinsicWidth()!=card.width || drawable.getIntrinsicHeight()!=card.height)){
				photo.setImageDrawable(null);
				photo.setImageDrawable(crossfadeDrawable);
			}
		}

		@Override
		public void clearImage(int index){
			crossfadeDrawable.setCrossfadeAlpha(1f);
			didClear=true;
		}

		private void onClick(View v){
			String url=item.status.card.url;
			// Mastodon.social sometimes adds an additional redirect page
			// this is really disruptive on mobile, especially since it breaks the loopUp/openURL functionality
			Uri parsedURL=Uri.parse(url);
			if(parsedURL.getPath()!=null && parsedURL.getPath().startsWith("/redirect/")){
				url=findRedirectedURL(parsedURL).orElse(url);
			}
			UiUtils.openURL(itemView.getContext(), item.parentFragment.getAccountID(), url);
		}

		private Optional<String> findRedirectedURL(Uri url){
			// find actually linked url in status content
			Matcher matcher=HtmlParser.URL_PATTERN.matcher(item.status.content);
			boolean isAccountRedirect=url.getPath().startsWith("/redirect/accounts");
			String foundURL;
			while(matcher.find()){
				foundURL=matcher.group(3);
				if(TextUtils.isEmpty(matcher.group(4)))
					foundURL="http://"+foundURL;
				// SAFETY: Cannot be null, as otherwise the matcher wouldn't find it
				// also, group is marked as non-null
				assert foundURL!=null && url.getLastPathSegment()!=null;
				if(foundURL.endsWith(url.getLastPathSegment()) ||
						(isAccountRedirect && foundURL.matches("https://"+url.getHost()+"/@[a-zA-Z0-9_]+@[a-zA-Z0-9._]+$"))){
					// found correct URL
					return Optional.of(foundURL);
				}
			}
			return Optional.empty();
		}
	}
}
