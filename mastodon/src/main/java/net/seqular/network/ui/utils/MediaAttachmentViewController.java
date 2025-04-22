package net.seqular.network.ui.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.R;
import net.seqular.network.model.Attachment;
import net.seqular.network.model.Status;
import net.seqular.network.ui.displayitems.MediaGridStatusDisplayItem;
import net.seqular.network.ui.drawables.BlurhashCrossfadeDrawable;
import net.seqular.network.ui.drawables.PlayIconDrawable;

public class MediaAttachmentViewController{
	public final View view;
	public final MediaGridStatusDisplayItem.GridItemType type;
	public final ImageView photo;
	public final View altButton, noAltButton, btnsWrap, extraBadge;
	public final TextView duration;
	public final View playButton;
	private BlurhashCrossfadeDrawable crossfadeDrawable=new BlurhashCrossfadeDrawable();
	private final Context context;
	private boolean didClear;
	private Status status;
	private Attachment attachment;

	public MediaAttachmentViewController(Context context, MediaGridStatusDisplayItem.GridItemType type){
		view=context.getSystemService(LayoutInflater.class).inflate(switch(type){
				case PHOTO -> R.layout.display_item_photo;
				case VIDEO -> R.layout.display_item_video;
				case GIFV -> R.layout.display_item_gifv;
			}, null);
		photo=view.findViewById(R.id.photo);
		altButton=view.findViewById(R.id.alt_button);
		noAltButton=view.findViewById(R.id.no_alt_button);
		btnsWrap=view.findViewById(R.id.alt_badges);
		duration=view.findViewById(R.id.duration);
		playButton=view.findViewById(R.id.play_button);
		extraBadge=view.findViewById(R.id.extra_badge);
		this.type=type;
		this.context=context;
		if(playButton!=null){
			// https://developer.android.com/topic/performance/hardware-accel#drawing-support
			if(Build.VERSION.SDK_INT<28)
				playButton.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			playButton.setBackground(new PlayIconDrawable(context));
		}
	}

	public void bind(Attachment attachment, Status status){
		this.status=status;
		this.attachment=attachment;
		crossfadeDrawable.setSize(attachment.getWidth(), attachment.getHeight());
		crossfadeDrawable.setBlurhashDrawable(attachment.blurhashPlaceholder);
		crossfadeDrawable.setCrossfadeAlpha(0f);
		photo.setImageDrawable(null);
		photo.setImageDrawable(crossfadeDrawable);
		boolean hasAltText = !TextUtils.isEmpty(attachment.description);
		photo.setContentDescription(!hasAltText ? context.getString(R.string.media_no_description) : attachment.description);
		if(btnsWrap!=null){
			btnsWrap.setVisibility(View.VISIBLE);
			altButton.setVisibility(hasAltText && GlobalUserPreferences.showAltIndicator ? View.VISIBLE : View.GONE);
			noAltButton.setVisibility(!hasAltText && GlobalUserPreferences.showNoAltIndicator ? View.VISIBLE : View.GONE);
		}
		if(type==MediaGridStatusDisplayItem.GridItemType.VIDEO){
			duration.setText(UiUtils.formatMediaDuration((int)attachment.getDuration()));
		}
		didClear=false;
	}

	public void setImage(Drawable drawable){
		crossfadeDrawable.setImageDrawable(drawable);
		if(didClear)
			 crossfadeDrawable.animateAlpha(0f);
		// Make sure the image is not stretched if the server returned wrong dimensions
		if(drawable!=null && (drawable.getIntrinsicWidth()!=attachment.getWidth() || drawable.getIntrinsicHeight()!=attachment.getHeight())){
			photo.setImageDrawable(null);
			photo.setImageDrawable(crossfadeDrawable);
		}
	}

	public void clearImage(){
		crossfadeDrawable.setCrossfadeAlpha(1f);
		crossfadeDrawable.setImageDrawable(null);
		didClear=true;
	}
}
