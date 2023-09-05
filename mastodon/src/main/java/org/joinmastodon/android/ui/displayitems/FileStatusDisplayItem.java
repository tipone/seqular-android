package org.joinmastodon.android.ui.displayitems;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.model.Attachment;
import org.joinmastodon.android.model.Card;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.SingleImagePhotoViewerListener;
import org.joinmastodon.android.ui.drawables.BlurhashCrossfadeDrawable;
import org.joinmastodon.android.ui.photoviewer.PhotoViewer;
import org.joinmastodon.android.ui.photoviewer.PhotoViewerHost;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import me.grishka.appkit.Nav;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.V;

public class FileStatusDisplayItem extends StatusDisplayItem{
    private final Status status;
    private final Attachment attachment;

    public FileStatusDisplayItem(String parentID, BaseStatusListFragment parentFragment, Attachment attachment, Status status){
        super(parentID, parentFragment);
        this.status=status;
        this.attachment=attachment;
    }

    @Override
    public Type getType(){
        return Type.FILE;
    }

    public static class Holder extends StatusDisplayItem.Holder<FileStatusDisplayItem>{
        private final TextView title, domain;
		private final View inner;
		private final ImageView icon;
		private final Context context;

		private PhotoViewer currentPhotoViewer;

        public Holder(Context context, ViewGroup parent){
            super(context, R.layout.display_item_file, parent);
            title=findViewById(R.id.title);
            domain=findViewById(R.id.domain);
			icon=findViewById(R.id.imageView);
			inner=findViewById(R.id.inner);
			this.context=context;

            findViewById(R.id.inner).setOnClickListener(this::onClick);
        }

        @Override
        public void onBind(FileStatusDisplayItem item) {
            Uri url = Uri.parse(getUrl());

			if(!item.attachment.type.isImage()) {
				title.setText(item.attachment.description != null
						? item.attachment.description
						: url.getLastPathSegment());

				title.setEllipsize(item.attachment.description != null ? TextUtils.TruncateAt.END : TextUtils.TruncateAt.MIDDLE);
				domain.setText(url.getHost());

				icon.setImageDrawable(context.getDrawable(R.drawable.ic_fluent_attach_24_regular));
			} else {
				title.setText(item.attachment.description != null
						? item.attachment.description
						: context.getString(R.string.sk_no_alt_text));
				title.setSingleLine(false);

				domain.setText(item.status.sensitive ? context.getString(R.string.sensitive_content_explain) : null);
				domain.setVisibility(item.status.sensitive ? View.VISIBLE : View.GONE);

				if(item.attachment.type == Attachment.Type.IMAGE)
					icon.setImageDrawable(context.getDrawable(R.drawable.ic_fluent_image_24_regular));
				if(item.attachment.type == Attachment.Type.VIDEO)
					icon.setImageDrawable(context.getDrawable(R.drawable.ic_fluent_video_clip_24_regular));
				if(item.attachment.type == Attachment.Type.GIFV)
					icon.setImageDrawable(context.getDrawable(R.drawable.ic_fluent_gif_24_regular));

			}
        }

        private void onClick(View v) {
			if(!item.attachment.type.isImage()) {
				UiUtils.openURL(itemView.getContext(), item.parentFragment.getAccountID(), getUrl());
			} else {
				List<Attachment> attachmentArray = new ArrayList<>();
				attachmentArray.add(item.attachment);

				currentPhotoViewer=new PhotoViewer((Activity) context, attachmentArray, 0,
						new SingleImagePhotoViewerListener(title, inner, new int[]{V.dp(28), V.dp(28), V.dp(28), V.dp(28)}, item.parentFragment, ()->currentPhotoViewer=null, ()->context.getDrawable(R.drawable.bg_search_field), null, null));
			}
        }

        private String getUrl() {
            return item.attachment.remoteUrl == null ? item.attachment.url : item.attachment.remoteUrl;
        }
    }
}

