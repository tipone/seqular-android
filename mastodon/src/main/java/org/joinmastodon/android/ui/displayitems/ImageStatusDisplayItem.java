package org.joinmastodon.android.ui.displayitems;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.model.Attachment;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.PhotoLayoutHelper;
import org.joinmastodon.android.ui.drawables.BlurhashCrossfadeDrawable;
import org.joinmastodon.android.ui.photoviewer.PhotoViewerHost;
import org.joinmastodon.android.ui.views.ImageAttachmentFrameLayout;

import androidx.annotation.LayoutRes;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.utils.CubicBezierInterpolator;

public abstract class ImageStatusDisplayItem extends StatusDisplayItem{
	public final int index;
	public final int totalPhotos;
	protected Attachment attachment;
	protected ImageLoaderRequest request;
	public final Status status;
	public final PhotoLayoutHelper.TiledLayoutResult tiledLayout;
	public final PhotoLayoutHelper.TiledLayoutResult.Tile thisTile;
	public int horizontalInset;

	public ImageStatusDisplayItem(String parentID, BaseStatusListFragment parentFragment, Attachment photo, Status status, int index, int totalPhotos, PhotoLayoutHelper.TiledLayoutResult tiledLayout, PhotoLayoutHelper.TiledLayoutResult.Tile thisTile){
		super(parentID, parentFragment);
		this.attachment=photo;
		this.status=status;
		this.index=index;
		this.totalPhotos=totalPhotos;
		this.tiledLayout=tiledLayout;
		this.thisTile=thisTile;
	}

	@Override
	public int getImageCount(){
		return 1;
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return request;
	}

	public static abstract class Holder<T extends ImageStatusDisplayItem> extends StatusDisplayItem.Holder<T> implements ImageLoaderViewHolder{
		public final ImageView photo;
		private ImageAttachmentFrameLayout layout;
		private BlurhashCrossfadeDrawable crossfadeDrawable=new BlurhashCrossfadeDrawable();
		private boolean didClear;

		private AnimatorSet currentAnim;
		private final FrameLayout altTextWrapper;
		private final TextView altTextButton;
		private final ImageView noAltTextButton;
		private final View altTextScroller;
		private final ImageButton altTextClose;
		private final TextView altText, noAltText;

		private View altOrNoAltButton;
		private boolean altTextShown;

		public Holder(Activity activity, @LayoutRes int layout, ViewGroup parent){
			super(activity, layout, parent);
			photo=findViewById(R.id.photo);
			photo.setOnClickListener(this::onViewClick);
			this.layout=(ImageAttachmentFrameLayout)itemView;

			altTextWrapper=findViewById(R.id.alt_text_wrapper);
			altTextButton=findViewById(R.id.alt_button);
			noAltTextButton=findViewById(R.id.no_alt_button);
			altTextScroller=findViewById(R.id.alt_text_scroller);
			altTextClose=findViewById(R.id.alt_text_close);
			altText=findViewById(R.id.alt_text);
			noAltText=findViewById(R.id.no_alt_text);

			altTextButton.setOnClickListener(this::onShowHideClick);
			noAltTextButton.setOnClickListener(this::onShowHideClick);
			altTextClose.setOnClickListener(this::onShowHideClick);
//			altTextScroller.setNestedScrollingEnabled(true);
		}

		@Override
		public void onBind(ImageStatusDisplayItem item){
			layout.setLayout(item.tiledLayout, item.thisTile, item.horizontalInset);
			crossfadeDrawable.setSize(item.attachment.getWidth(), item.attachment.getHeight());
			crossfadeDrawable.setBlurhashDrawable(item.attachment.blurhashPlaceholder);
			crossfadeDrawable.setCrossfadeAlpha(item.status.spoilerRevealed ? 0f : 1f);
			photo.setImageDrawable(null);
			photo.setImageDrawable(crossfadeDrawable);
			photo.setContentDescription(TextUtils.isEmpty(item.attachment.description) ? item.parentFragment.getString(R.string.media_no_description) : item.attachment.description);
			didClear=false;

			if (currentAnim != null) currentAnim.cancel();

			boolean altTextMissing = TextUtils.isEmpty(item.attachment.description);
			altOrNoAltButton = altTextMissing ? noAltTextButton : altTextButton;
			altTextShown=false;

			altTextScroller.setVisibility(View.GONE);
			altTextClose.setVisibility(View.GONE);
			altTextButton.setVisibility(View.VISIBLE);
			noAltTextButton.setVisibility(View.VISIBLE);
			altTextButton.setAlpha(1f);
			noAltTextButton.setAlpha(1f);
			altTextWrapper.setVisibility(View.VISIBLE);

			if (altTextMissing){
				if (GlobalUserPreferences.showNoAltIndicator) {
					noAltTextButton.setVisibility(View.VISIBLE);
					noAltText.setVisibility(View.VISIBLE);
					altTextWrapper.setBackgroundResource(R.drawable.bg_image_no_alt_overlay);
					altTextButton.setVisibility(View.GONE);
					altText.setVisibility(View.GONE);
				} else {
					altTextWrapper.setVisibility(View.GONE);
				}
			}else{
				if (GlobalUserPreferences.showAltIndicator) {
					noAltTextButton.setVisibility(View.GONE);
					noAltText.setVisibility(View.GONE);
					altTextWrapper.setBackgroundResource(R.drawable.bg_image_alt_overlay);
					altTextButton.setVisibility(View.VISIBLE);
					altTextButton.setText(R.string.sk_alt_button);
					altText.setVisibility(View.VISIBLE);
					altText.setText(item.attachment.description);
					altText.setPadding(0, 0, 0, 0);
				} else {
					altTextWrapper.setVisibility(View.GONE);
				}
			}
		}

		private void onShowHideClick(View v){
			boolean show=v.getId()==R.id.alt_button || v.getId()==R.id.no_alt_button;

			if(altTextShown==show)
				return;
			if(currentAnim!=null)
				currentAnim.cancel();

			altTextShown=show;
			if(show){
				altTextScroller.setVisibility(View.VISIBLE);
				altTextClose.setVisibility(View.VISIBLE);
			}else{
				altOrNoAltButton.setVisibility(View.VISIBLE);
				// Hide these views temporarily so FrameLayout measures correctly
				altTextScroller.setVisibility(View.GONE);
				altTextClose.setVisibility(View.GONE);
			}

			// This is the current size...
			int prevLeft=altTextWrapper.getLeft();
			int prevRight=altTextWrapper.getRight();
			int prevTop=altTextWrapper.getTop();
			altTextWrapper.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
				@Override
				public boolean onPreDraw(){
					altTextWrapper.getViewTreeObserver().removeOnPreDrawListener(this);

					// ...and this is after the layout pass, right now the FrameLayout has its final size, but we animate that change
					if(!show){
						// Show these views again so they're visible for the duration of the animation.
						// No one would notice they were missing during measure/layout.
						altTextScroller.setVisibility(View.VISIBLE);
						altTextClose.setVisibility(View.VISIBLE);
					}
					AnimatorSet set=new AnimatorSet();
					set.playTogether(
							ObjectAnimator.ofInt(altTextWrapper, "left", prevLeft, altTextWrapper.getLeft()),
							ObjectAnimator.ofInt(altTextWrapper, "right", prevRight, altTextWrapper.getRight()),
							ObjectAnimator.ofInt(altTextWrapper, "top", prevTop, altTextWrapper.getTop()),
							ObjectAnimator.ofFloat(altOrNoAltButton, View.ALPHA, show ? 1f : 0f, show ? 0f : 1f),
							ObjectAnimator.ofFloat(altTextScroller, View.ALPHA, show ? 0f : 1f, show ? 1f : 0f),
							ObjectAnimator.ofFloat(altTextClose, View.ALPHA, show ? 0f : 1f, show ? 1f : 0f)
					);
					set.setDuration(300);
					set.setInterpolator(CubicBezierInterpolator.DEFAULT);
					set.addListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							if(show){
								altOrNoAltButton.setVisibility(View.GONE);
							}else{
								altTextScroller.setVisibility(View.GONE);
								altTextClose.setVisibility(View.GONE);
							}
							currentAnim=null;
						}
					});
					set.start();
					currentAnim=set;

					return true;
				}
			});
		}

		@Override
		public void setImage(int index, Drawable drawable){
			crossfadeDrawable.setImageDrawable(drawable);
			if(didClear && item.status.spoilerRevealed)
				crossfadeDrawable.animateAlpha(0f);
		}

		@Override
		public void clearImage(int index){
			crossfadeDrawable.setCrossfadeAlpha(1f);
			crossfadeDrawable.setImageDrawable(null);
			didClear=true;
		}

		private void onViewClick(View v){
			if(!item.status.spoilerRevealed){
				item.parentFragment.onRevealSpoilerClick(this);
			}else if(item.parentFragment instanceof PhotoViewerHost){
				Status contentStatus=item.status.reblog!=null ? item.status.reblog : item.status;
				((PhotoViewerHost) item.parentFragment).openPhotoViewer(item.parentID, item.status, contentStatus.mediaAttachments.indexOf(item.attachment));
			}
		}

		public void setRevealed(boolean revealed){
			crossfadeDrawable.animateAlpha(revealed ? 0f : 1f);
		}
	}
}
