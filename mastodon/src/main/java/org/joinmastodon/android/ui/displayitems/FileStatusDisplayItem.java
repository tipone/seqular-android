package org.joinmastodon.android.ui.displayitems;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import org.joinmastodon.android.ui.drawables.BlurhashCrossfadeDrawable;
import org.joinmastodon.android.ui.utils.UiUtils;

import me.grishka.appkit.imageloader.ImageLoaderViewHolder;

public class FileStatusDisplayItem extends StatusDisplayItem{
    private final Attachment attachment;

    public FileStatusDisplayItem(String parentID, BaseStatusListFragment<?> parentFragment, Attachment attachment) {
        super(parentID, parentFragment);
        this.attachment=attachment;
    }

    @Override
    public Type getType() {
        return Type.FILE;
    }

    public static class Holder extends StatusDisplayItem.Holder<FileStatusDisplayItem> {
        private final TextView title, domain;

        public Holder(Context context, ViewGroup parent) {
            super(context, R.layout.display_item_file, parent);
            title=findViewById(R.id.title);
            domain=findViewById(R.id.domain);
            findViewById(R.id.inner).setOnClickListener(this::onClick);
        }

        @Override
        public void onBind(FileStatusDisplayItem item) {
            Uri url = Uri.parse(item.attachment.remoteUrl);
            title.setText(item.attachment.description != null
                    ? item.attachment.description
                    : url.getLastPathSegment());
            title.setEllipsize(item.attachment.description != null ? TextUtils.TruncateAt.END : TextUtils.TruncateAt.MIDDLE);
            domain.setText(url.getHost());
        }

        private void onClick(View v) {
            UiUtils.openURL(itemView.getContext(), item.parentFragment.getAccountID(), item.attachment.remoteUrl);
        }
    }
}

