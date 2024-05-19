package org.joinmastodon.android.ui.displayitems;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.model.Attachment;
import org.joinmastodon.android.ui.utils.UiUtils;

public class ErrorStatusDisplayItem extends StatusDisplayItem{
	private final Exception exception;

	public ErrorStatusDisplayItem(String parentID, BaseStatusListFragment<?> parentFragment, Exception exception) {
		super(parentID, parentFragment);
		this.exception=exception;
	}

	@Override
	public Type getType() {
		return Type.ERROR_ITEM;
	}

	public static class Holder extends StatusDisplayItem.Holder<ErrorStatusDisplayItem> {
		private final TextView title, domain;

		public Holder(Context context, ViewGroup parent) {
			super(context, R.layout.display_item_file, parent);
			title=findViewById(R.id.title);
			domain=findViewById(R.id.domain);
			findViewById(R.id.inner).setOnClickListener(this::onClick);
		}

		@Override
		public void onBind(ErrorStatusDisplayItem item) {
			title.setText(item.exception.getMessage());
//			title.setEllipsize(item.attachment.description != null ? TextUtils.TruncateAt.END : TextUtils.TruncateAt.MIDDLE);
//			domain.setText(url.getHost());
		}

		private void onClick(View v) {
//			UiUtils.openURL(itemView.getContext(), item.parentFragment.getAccountID(), getUrl());
		}
	}
}

