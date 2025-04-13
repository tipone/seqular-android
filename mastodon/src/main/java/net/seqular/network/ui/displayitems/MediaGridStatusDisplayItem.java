package net.seqular.network.ui.displayitems;

import static net.seqular.network.GlobalUserPreferences.*;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.seqular.network.R;
import net.seqular.network.fragments.BaseStatusListFragment;
import net.seqular.network.model.Attachment;
import net.seqular.network.model.Status;
import net.seqular.network.model.Translation;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.PhotoLayoutHelper;
import net.seqular.network.ui.drawables.SpoilerStripesDrawable;
import net.seqular.network.ui.photoviewer.PhotoViewerHost;
import net.seqular.network.ui.utils.MediaAttachmentViewController;
import net.seqular.network.ui.utils.UiUtils;
import net.seqular.network.ui.views.FrameLayoutThatOnlyMeasuresFirstChild;
import net.seqular.network.ui.views.MaxWidthFrameLayout;
import net.seqular.network.ui.views.MediaGridLayout;
import net.seqular.network.utils.TypedObjectPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.CubicBezierInterpolator;
import me.grishka.appkit.utils.V;

public class MediaGridStatusDisplayItem extends StatusDisplayItem{
	private static final String TAG="MediaGridDisplayItem";

	private PhotoLayoutHelper.TiledLayoutResult tiledLayout;
	private final TypedObjectPool<GridItemType, MediaAttachmentViewController> viewPool;
	private final List<Attachment> attachments;
	private final Map<String, Pair<String, String>> translatedAttachments = new HashMap<>();
	private final ArrayList<ImageLoaderRequest> requests=new ArrayList<>();
	public String sensitiveTitle;

	public MediaGridStatusDisplayItem(String parentID, BaseStatusListFragment<?> parentFragment, PhotoLayoutHelper.TiledLayoutResult tiledLayout, List<Attachment> attachments, Status status){
		super(parentID, parentFragment);
		this.tiledLayout=tiledLayout;
		this.viewPool=parentFragment.getAttachmentViewsPool();
		this.attachments=attachments;
		this.status=status;
		for(Attachment att:attachments){
			requests.add(new UrlImageLoaderRequest(switch(att.type){
				case IMAGE -> att.url;
				case VIDEO, GIFV -> att.previewUrl == null ? att.url : att.previewUrl;
				default -> throw new IllegalStateException("Unexpected value: "+att.url);
			}, 1000, 1000));
		}
	}

	@Override
	public Type getType(){
		return Type.MEDIA_GRID;
	}

	@Override
	public int getImageCount(){
		return requests.size();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return requests.get(index);
	}

	public enum GridItemType{
		PHOTO,
		VIDEO,
		GIFV
	}

	public static class Holder extends StatusDisplayItem.Holder<MediaGridStatusDisplayItem> implements ImageLoaderViewHolder{
		private final FrameLayout wrapper;
		private final MediaGridLayout layout;
		private final View.OnClickListener clickListener=this::onViewClick, altTextClickListener=this::onAltTextClick;
		private final ArrayList<MediaAttachmentViewController> controllers=new ArrayList<>();

		private final MaxWidthFrameLayout overlays;
		private final FrameLayout altTextWrapper;
		private final TextView altTextButton;
		private final ImageView noAltTextButton;
		private final View altTextScroller;
		private final ImageButton altTextClose;
		private final TextView altText, noAltText;

		private final View sensitiveOverlay;
		private final LayerDrawable sensitiveOverlayBG;
		private static final ColorDrawable drawableForWhenThereIsNoBlurhash=new ColorDrawable(0xffffffff);
//		private final FrameLayout hideSensitiveButton;
		private final TextView sensitiveText;

		private int altTextIndex=-1;
		private Animator altTextAnimator;

		public Holder(Activity activity, ViewGroup parent){
			super(new FrameLayoutThatOnlyMeasuresFirstChild(activity));
			wrapper=(FrameLayout)itemView;
			layout=new MediaGridLayout(activity);
			wrapper.addView(layout);
			wrapper.setClipToPadding(false);

			overlays=new MaxWidthFrameLayout(activity);
			overlays.setMaxWidth(UiUtils.MAX_WIDTH);
			wrapper.addView(overlays, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL));

			activity.getLayoutInflater().inflate(R.layout.overlay_image_alt_text, overlays);
			altTextWrapper=findViewById(R.id.alt_text_wrapper);
			altTextButton=findViewById(R.id.alt_button);
			noAltTextButton=findViewById(R.id.no_alt_button);
			altTextScroller=findViewById(R.id.alt_text_scroller);
			altTextClose=findViewById(R.id.alt_text_close);
			altText=findViewById(R.id.alt_text);
			noAltText=findViewById(R.id.no_alt_text);
			altTextClose.setOnClickListener(this::onAltTextCloseClick);

			// megalodon: no sensitive hide button because the visibility toggle looks prettier imo
//			hideSensitiveButton=(FrameLayout) activity.getLayoutInflater().inflate(R.layout.alt_text_badge, overlays, false);
//			((TextView) hideSensitiveButton.findViewById(R.id.alt_button)).setText(R.string.hide);
//			overlays.addView(hideSensitiveButton, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.TOP));

			activity.getLayoutInflater().inflate(R.layout.overlay_image_sensitive, overlays);
			sensitiveOverlay=findViewById(R.id.sensitive_overlay);
			sensitiveOverlayBG=(LayerDrawable) sensitiveOverlay.getBackground().mutate();
			sensitiveOverlayBG.setDrawableByLayerId(R.id.left_drawable, new SpoilerStripesDrawable(false));
			sensitiveOverlayBG.setDrawableByLayerId(R.id.right_drawable, new SpoilerStripesDrawable(true));
			sensitiveOverlay.setBackground(sensitiveOverlayBG);
			sensitiveOverlay.setOnClickListener(v->revealSensitive());
//			hideSensitiveButton.setOnClickListener(v->hideSensitive());

			sensitiveText=findViewById(R.id.sensitive_text);
		}

		@Override
		public void onBind(MediaGridStatusDisplayItem item){
			wrapper.setPadding(0, 0, 0, 0); // item.inset ? 0 : V.dp(8));

			if(altTextAnimator!=null)
				altTextAnimator.cancel();

			layout.setTiledLayout(item.tiledLayout);
			for(MediaAttachmentViewController c:controllers){
				item.viewPool.reuse(c.type, c);
			}
			layout.removeAllViews();
			controllers.clear();

			int i=0;
			if (!item.attachments.isEmpty()) updateBlurhashInSensitiveOverlay();
			for(Attachment att:item.attachments){
				MediaAttachmentViewController c=item.viewPool.obtain(switch(att.type){
					case IMAGE -> GridItemType.PHOTO;
					case VIDEO -> GridItemType.VIDEO;
					case GIFV -> GridItemType.GIFV;
					default -> throw new IllegalStateException("Unexpected value: "+att.type);
				});
				if(c.view.getLayoutParams()==null)
					c.view.setLayoutParams(new MediaGridLayout.LayoutParams(item.tiledLayout.tiles[i]));
				else
					((MediaGridLayout.LayoutParams) c.view.getLayoutParams()).tile=item.tiledLayout.tiles[i];
				layout.addView(c.view);
				c.view.setOnClickListener(clickListener);
				c.view.setTag(i);
				if(c.btnsWrap!=null){
					c.btnsWrap.setOnClickListener(altTextClickListener);
					c.btnsWrap.setTag(i);
					c.btnsWrap.setAlpha(1f);
				}
				controllers.add(c);

				if (item.status.translation != null){
					if(item.status.translationState==Status.TranslationState.SHOWN){
						if(!item.translatedAttachments.containsKey(att.id)){
							Optional<Translation.MediaAttachment> translatedAttachment=Arrays.stream(item.status.translation.mediaAttachments).filter(mediaAttachment->mediaAttachment.id.equals(att.id)).findFirst();
							translatedAttachment.ifPresent(mediaAttachment->{
								item.translatedAttachments.put(mediaAttachment.id, new Pair<>(att.description, mediaAttachment.description));
								att.description=mediaAttachment.description;
							});
						}else{
							//SAFETY: must be non-null, as we check if the map contains the attachment before
							att.description=Objects.requireNonNull(item.translatedAttachments.get(att.id)).second;
						}
					}else{
						if (item.translatedAttachments.containsKey(att.id)) {
							att.description=Objects.requireNonNull(item.translatedAttachments.get(att.id)).first;
						}
					}
				}
				c.bind(att, item.status);
				i++;
			}
			altTextButton.setVisibility(View.VISIBLE);
			noAltTextButton.setVisibility(View.VISIBLE);
			altTextWrapper.setVisibility(View.GONE);
			altTextIndex=-1;

			if(!item.status.sensitiveRevealed){
				sensitiveOverlay.setVisibility(View.VISIBLE);
				layout.setVisibility(View.INVISIBLE);
			}else{
				sensitiveOverlay.setVisibility(View.GONE);
				layout.setVisibility(View.VISIBLE);
			}
//			hideSensitiveButton.setVisibility(item.status.sensitive ? View.VISIBLE : View.GONE);
			if(!TextUtils.isEmpty(item.sensitiveTitle))
				sensitiveText.setText(item.sensitiveTitle);
			else if (!item.status.sensitive)
				sensitiveText.setText(R.string.media_hidden);
			else
				sensitiveText.setText(R.string.sensitive_content_explain);

			boolean insetAndLast=item.inset && isLastDisplayItemForStatus();
			wrapper.setClipToOutline(insetAndLast);
			wrapper.setOutlineProvider(insetAndLast ? OutlineProviders.bottomRoundedRect(12) : null);
		}

		@Override
		public void setImage(int index, Drawable drawable){
			if(item.attachments.get(index).meta==null){
				Rect bounds=drawable.getBounds();
				drawable.setBounds(bounds.left, bounds.top, bounds.left+drawable.getIntrinsicWidth(), bounds.top+drawable.getIntrinsicHeight());
				Attachment.Metadata metadata = new Attachment.Metadata();
				metadata.width=drawable.getIntrinsicWidth();
				metadata.height=drawable.getIntrinsicHeight();
				item.attachments.get(index).meta=metadata;
				item.tiledLayout=PhotoLayoutHelper.processThumbs(item.attachments);
				UiUtils.beginLayoutTransition((ViewGroup) itemView);
				rebind();
			}
			controllers.get(index).setImage(drawable);
		}

		@Override
		public void clearImage(int index){
			controllers.get(index).clearImage();
		}

		private void onViewClick(View v){
			int index=(Integer)v.getTag();
			((PhotoViewerHost) item.parentFragment).openPhotoViewer(item.parentID, item.status, index, this);
		}

		private void onAltTextClick(View v){
			if(altTextAnimator!=null)
				altTextAnimator.cancel();
//			V.setVisibilityAnimated(hideSensitiveButton, View.GONE);
			V.cancelVisibilityAnimation(altTextWrapper);
			v.setVisibility(View.INVISIBLE);
			int index=(Integer)v.getTag();
			altTextIndex=index;
			Attachment att=item.attachments.get(index);
			boolean hasAltText = !TextUtils.isEmpty(att.description);
			if ((hasAltText && !showAltIndicator) || (!hasAltText && !showNoAltIndicator)) return;
			altTextButton.setVisibility(hasAltText && showAltIndicator ? View.VISIBLE : View.GONE);
			noAltTextButton.setVisibility(!hasAltText && showNoAltIndicator ? View.VISIBLE : View.GONE);
			altText.setVisibility(hasAltText && showAltIndicator ? View.VISIBLE : View.GONE);
			noAltText.setVisibility(!hasAltText && showNoAltIndicator ? View.VISIBLE : View.GONE);
			altText.setText(att.description);
			altTextWrapper.setVisibility(View.VISIBLE);
			altTextWrapper.setBackgroundResource(hasAltText ? R.drawable.bg_image_alt_text_overlay : R.drawable.bg_image_no_alt_overlay);
			altTextWrapper.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
				@Override
				public boolean onPreDraw(){
					altTextWrapper.getViewTreeObserver().removeOnPreDrawListener(this);

					int[] loc={0, 0};
					v.getLocationInWindow(loc);
					int btnL=loc[0], btnT=loc[1];
					overlays.getLocationInWindow(loc);
					btnL-=loc[0];
					btnT-=loc[1];

					ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) altTextWrapper.getLayoutParams();
					ArrayList<Animator> anims=new ArrayList<>();
					anims.add(ObjectAnimator.ofFloat(altTextButton, View.ALPHA, 1, 0));
					anims.add(ObjectAnimator.ofFloat(noAltTextButton, View.ALPHA, 1, 0));
					anims.add(ObjectAnimator.ofFloat(altTextScroller, View.ALPHA, 0, 1));
					anims.add(ObjectAnimator.ofFloat(altTextClose, View.ALPHA, 0, 1));
					anims.add(ObjectAnimator.ofInt(altTextWrapper, "left", btnL+margins.leftMargin, altTextWrapper.getLeft()));
					anims.add(ObjectAnimator.ofInt(altTextWrapper, "top", btnT+margins.topMargin, altTextWrapper.getTop()));
					anims.add(ObjectAnimator.ofInt(altTextWrapper, "right", btnL+v.getWidth()-margins.rightMargin, altTextWrapper.getRight()));
					anims.add(ObjectAnimator.ofInt(altTextWrapper, "bottom", btnT+v.getHeight()-margins.bottomMargin, altTextWrapper.getBottom()));
					for(Animator a:anims)
						a.setDuration(300);

					for(MediaAttachmentViewController c:controllers){
						if(c.btnsWrap!=null && c.btnsWrap!=v){
							anims.add(ObjectAnimator.ofFloat(c.btnsWrap, View.ALPHA, 1, 0).setDuration(150));
						}
						if (c.extraBadge != null) {
							anims.add(ObjectAnimator.ofFloat(c.extraBadge, View.ALPHA, 1, 0).setDuration(150));
						}
					}

					AnimatorSet set=new AnimatorSet();
					set.playTogether(anims);
					set.setInterpolator(CubicBezierInterpolator.DEFAULT);
					set.addListener(new AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator animation){
							altTextAnimator=null;
							for(MediaAttachmentViewController c:controllers){
								if(c.btnsWrap!=null){
									c.btnsWrap.setVisibility(View.INVISIBLE);
								}
								if (c.extraBadge != null) c.extraBadge.setVisibility(View.INVISIBLE);
							}
						}
					});
					altTextAnimator=set;
					set.start();

					return true;
				}
			});
		}

		private void onAltTextCloseClick(View v){
			if(altTextAnimator!=null)
				altTextAnimator.cancel();

//			V.setVisibilityAnimated(hideSensitiveButton, item.status.sensitive ? View.VISIBLE : View.GONE);
			V.cancelVisibilityAnimation(altTextWrapper);
			View btn=controllers.get(altTextIndex).btnsWrap;
			int i=0;
			for(MediaAttachmentViewController c:controllers){
				boolean hasAltText = !TextUtils.isEmpty(item.attachments.get(i).description);
				if(c.btnsWrap!=null
						&& c.btnsWrap!=btn
						&& ((hasAltText && showAltIndicator) || (!hasAltText && showNoAltIndicator))
				) c.btnsWrap.setVisibility(View.VISIBLE);
				if (c.extraBadge != null) c.extraBadge.setVisibility(View.VISIBLE);
				i++;
			}

			int[] loc={0, 0};
			btn.getLocationInWindow(loc);
			int btnL=loc[0], btnT=loc[1];
			overlays.getLocationInWindow(loc);
			btnL-=loc[0];
			btnT-=loc[1];

			ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) altTextWrapper.getLayoutParams();
			ArrayList<Animator> anims=new ArrayList<>();
			anims.add(ObjectAnimator.ofFloat(altTextButton, View.ALPHA, 1));
			anims.add(ObjectAnimator.ofFloat(noAltTextButton, View.ALPHA, 1));
			anims.add(ObjectAnimator.ofFloat(altTextScroller, View.ALPHA, 0));
			anims.add(ObjectAnimator.ofFloat(altTextClose, View.ALPHA, 0));
			anims.add(ObjectAnimator.ofInt(altTextWrapper, "left", btnL+margins.leftMargin));
			anims.add(ObjectAnimator.ofInt(altTextWrapper, "top", btnT+margins.topMargin));
			anims.add(ObjectAnimator.ofInt(altTextWrapper, "right", btnL+btn.getWidth()-margins.rightMargin));
			anims.add(ObjectAnimator.ofInt(altTextWrapper, "bottom", btnT+btn.getHeight()-margins.bottomMargin));
			for(Animator a:anims)
				a.setDuration(300);

			for(MediaAttachmentViewController c:controllers){
				if(c.btnsWrap!=null && c.btnsWrap!=btn){
					anims.add(ObjectAnimator.ofFloat(c.btnsWrap, View.ALPHA, 1).setDuration(150));
				}
				if (c.extraBadge != null) {
					anims.add(ObjectAnimator.ofFloat(c.extraBadge, View.ALPHA, 1).setDuration(150));
				}
			}

			AnimatorSet set=new AnimatorSet();
			set.playTogether(anims);
			set.setInterpolator(CubicBezierInterpolator.DEFAULT);
			set.addListener(new AnimatorListenerAdapter(){
				@Override
				public void onAnimationEnd(Animator animation){
					altTextAnimator=null;
					V.setVisibilityAnimated(altTextWrapper, View.GONE);
					V.setVisibilityAnimated(btn, View.VISIBLE);
					btn.setAlpha(1);
				}
			});
			altTextAnimator=set;
			set.start();
		}

		public MediaAttachmentViewController getViewController(int index){
			return index<controllers.size() ? controllers.get(index) : null;
		}

		public void setClipChildren(boolean clip){
			layout.setClipChildren(clip);
			wrapper.setClipChildren(clip);
		}

		private void updateBlurhashInSensitiveOverlay(){
			Drawable d = item.attachments.get(0).blurhashPlaceholder;
			sensitiveOverlayBG.setDrawableByLayerId(R.id.blurhash, d==null ? drawableForWhenThereIsNoBlurhash : d.mutate());
			sensitiveOverlay.setBackground(sensitiveOverlayBG);
		}

		public void revealSensitive(){
			if(item.status.sensitiveRevealed)
				return;
			item.status.sensitiveRevealed=true;
			V.setVisibilityAnimated(sensitiveOverlay, View.GONE);
			layout.setVisibility(View.VISIBLE);
			item.parentFragment.onSensitiveRevealed(this);
		}

		public void hideSensitive(){
			if(!item.status.sensitiveRevealed)
				return;
			updateBlurhashInSensitiveOverlay();
			item.status.sensitiveRevealed=false;
			V.setVisibilityAnimated(sensitiveOverlay, View.VISIBLE, ()->layout.setVisibility(View.INVISIBLE));
		}

		public MediaGridLayout getLayout(){
			return layout;
		}

		public View getSensitiveOverlay(){
			return sensitiveOverlay;
		}
	}
}
