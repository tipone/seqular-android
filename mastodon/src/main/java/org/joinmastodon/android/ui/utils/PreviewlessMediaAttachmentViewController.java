package org.joinmastodon.android.ui.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.R;
import org.joinmastodon.android.model.Attachment;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.displayitems.MediaGridStatusDisplayItem;
import org.joinmastodon.android.ui.drawables.BlurhashCrossfadeDrawable;
import org.joinmastodon.android.ui.drawables.PlayIconDrawable;

public class PreviewlessMediaAttachmentViewController{
	public final View view;
	public final MediaGridStatusDisplayItem.GridItemType type;
	private final TextView title, domain;
	public final View inner;
	private final ImageView icon;
	private final Context context;
	private Status status;

	public PreviewlessMediaAttachmentViewController(Context context, MediaGridStatusDisplayItem.GridItemType type){
		view=context.getSystemService(LayoutInflater.class).inflate(R.layout.display_item_file, null);
		title=view.findViewById(R.id.title);
		domain=view.findViewById(R.id.domain);
		icon=view.findViewById(R.id.imageView);
		inner=view.findViewById(R.id.inner);
		this.context=context;
		this.type=type;
	}

	public void bind(Attachment attachment, Status status){
		this.status=status;
		title.setText(attachment.description != null
				? attachment.description
				: context.getString(R.string.sk_no_alt_text));
		title.setSingleLine(false);

		domain.setText(status.sensitive ? context.getString(R.string.sensitive_content_explain) : null);
		domain.setVisibility(status.sensitive ? View.VISIBLE : View.GONE);

		if(attachment.type == Attachment.Type.IMAGE)
			icon.setImageDrawable(context.getDrawable(R.drawable.ic_fluent_image_24_regular));
		if(attachment.type == Attachment.Type.VIDEO)
			icon.setImageDrawable(context.getDrawable(R.drawable.ic_fluent_video_clip_24_regular));
		if(attachment.type == Attachment.Type.GIFV)
			icon.setImageDrawable(context.getDrawable(R.drawable.ic_fluent_gif_24_regular));
	}
}
