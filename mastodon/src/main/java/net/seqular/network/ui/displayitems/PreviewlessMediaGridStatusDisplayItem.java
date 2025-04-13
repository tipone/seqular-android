package net.seqular.network.ui.displayitems;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.seqular.network.fragments.BaseStatusListFragment;
import net.seqular.network.model.Attachment;
import net.seqular.network.model.Status;
import net.seqular.network.model.Translation;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.PhotoLayoutHelper;
import net.seqular.network.ui.utils.PreviewlessMediaAttachmentViewController;
import net.seqular.network.ui.views.FrameLayoutThatOnlyMeasuresFirstChild;
import net.seqular.network.utils.TypedObjectPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;

public class PreviewlessMediaGridStatusDisplayItem extends StatusDisplayItem{
	private static final String TAG="PreviewlessMediaGridDisplayItem";

	private PhotoLayoutHelper.TiledLayoutResult tiledLayout;
	private final TypedObjectPool<MediaGridStatusDisplayItem.GridItemType, PreviewlessMediaAttachmentViewController> viewPool;
	private final List<Attachment> attachments;
	private final Map<String, Pair<String, String>> translatedAttachments = new HashMap<>();
	private final ArrayList<ImageLoaderRequest> requests=new ArrayList<>();
	public final Status status;
	public String sensitiveTitle;

	public PreviewlessMediaGridStatusDisplayItem(String parentID, BaseStatusListFragment<?> parentFragment, PhotoLayoutHelper.TiledLayoutResult tiledLayout, List<Attachment> attachments, Status status){
		super(parentID, parentFragment);
		this.tiledLayout=tiledLayout;
		this.viewPool=parentFragment.getPreviewlessAttachmentViewsPool();
		this.attachments=attachments;
		this.status=status;
//		for(Attachment att:attachments){
//			requests.add(new UrlImageLoaderRequest(switch(att.type){
//				case IMAGE -> att.url;
//				case VIDEO, GIFV -> att.previewUrl == null ? att.url : att.previewUrl;
//				default -> throw new IllegalStateException("Unexpected value: "+att.url);
//			}, 1000, 1000));
//		}
	}

	@Override
	public Type getType(){
		return Type.PREVIEWLESS_MEDIA_GRID;
	}

	@Override
	public int getImageCount(){
		return attachments.size();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return requests.get(index);
	}

	public static class Holder extends StatusDisplayItem.Holder<PreviewlessMediaGridStatusDisplayItem> {
		private final FrameLayout wrapper;
		private final LinearLayout layout;
		private final View.OnClickListener clickListener=this::onViewClick;
		private final ArrayList<PreviewlessMediaAttachmentViewController> controllers=new ArrayList<>();

		//		private final FrameLayout hideSensitiveButton;

		public Holder(Activity activity, ViewGroup parent){
			super(new FrameLayoutThatOnlyMeasuresFirstChild(activity));
			wrapper=(FrameLayout)itemView;
			layout= new LinearLayout(activity);
			layout.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			layout.setLayoutParams(params);
			wrapper.addView(layout);
			wrapper.setClipToPadding(false);

			// megalodon: no sensitive hide button because the visibility toggle looks prettier imo
//			hideSensitiveButton=(FrameLayout) activity.getLayoutInflater().inflate(R.layout.alt_text_badge, overlays, false);
//			((TextView) hideSensitiveButton.findViewById(R.id.alt_button)).setText(R.string.hide);
//			overlays.addView(hideSensitiveButton, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.TOP));

//			hideSensitiveButton.setOnClickListener(v->hideSensitive());
		}

		@Override
		public void onBind(PreviewlessMediaGridStatusDisplayItem item){
			wrapper.setPadding(0, 0, 0, 0); // item.inset ? 0 : V.dp(8));

//			if(altTextAnimator!=null)
//				altTextAnimator.cancel();

			for(PreviewlessMediaAttachmentViewController c:controllers){
				item.viewPool.reuse(c.type, c);
			}
			layout.removeAllViews();
			controllers.clear();

			int i=0;
//			if (!item.attachments.isEmpty()) updateBlurhashInSensitiveOverlay();
			for(Attachment att:item.attachments){
				PreviewlessMediaAttachmentViewController c=item.viewPool.obtain(switch(att.type){
					case IMAGE -> MediaGridStatusDisplayItem.GridItemType.PHOTO;
					case VIDEO -> MediaGridStatusDisplayItem.GridItemType.VIDEO;
					case GIFV -> MediaGridStatusDisplayItem.GridItemType.GIFV;
					default -> throw new IllegalStateException("Unexpected value: "+att.type);
				});
				if(c.view.getLayoutParams()==null)
					c.view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				layout.addView(c.view);
				c.view.setOnClickListener(clickListener);
				c.view.setTag(i);
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

			boolean insetAndLast=item.inset && isLastDisplayItemForStatus();
			wrapper.setClipToOutline(insetAndLast);
			wrapper.setOutlineProvider(insetAndLast ? OutlineProviders.bottomRoundedRect(12) : null);
		}

		private void onViewClick(View v){
			int index=(Integer)v.getTag();
			item.parentFragment.openPreviewlessMediaPhotoViewer(item.parentID, item.status, index, this);
		}


		public PreviewlessMediaAttachmentViewController getViewController(int index){
			return controllers.get(index);
		}

		public void setClipChildren(boolean clip){
			layout.setClipChildren(clip);
			wrapper.setClipChildren(clip);
		}

		public LinearLayout getLayout(){
			return layout;
		}
	}
}
