package org.joinmastodon.android.ui.displayitems;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.joinmastodon.android.BuildConfig;
import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorStatusDisplayItem extends StatusDisplayItem{
	private final Exception exception;

	public ErrorStatusDisplayItem(String parentID, Status status, BaseStatusListFragment<?> parentFragment, Exception exception) {
		super(parentID, parentFragment);
		this.exception=exception;
		this.status=status;
	}

	@Override
	public Type getType() {
		return Type.ERROR_ITEM;
	}

	public static class Holder extends StatusDisplayItem.Holder<ErrorStatusDisplayItem> {

		public Holder(Context context, ViewGroup parent) {
			super(context, R.layout.display_item_error, parent);
			Button openInBrowserButton=findViewById(R.id.button_open_browser);
			openInBrowserButton.setEnabled(item.status.url!=null);
			openInBrowserButton.setOnClickListener(v -> UiUtils.launchWebBrowser(v.getContext(), item.status.url));
			findViewById(R.id.button_copy_error_details).setOnClickListener(this::copyErrorDetails);
		}

		@Override
		public void onBind(ErrorStatusDisplayItem item) {}

		private void copyErrorDetails(View v) {
			StringWriter stringWriter=new StringWriter();
			PrintWriter printWriter=new PrintWriter(stringWriter);
			item.exception.printStackTrace(printWriter);
			String stackTrace=stringWriter.toString();

			String errorDetails=String.format(
					"App Version: %s\nOS Version: %s\nStatus URL: %s\nException: %s",
					v.getContext().getString(R.string.mo_settings_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
					"Android " + Build.VERSION.RELEASE,
					item.status.url,
					stackTrace
			);
			UiUtils.copyText(v, errorDetails);
		}
	}
}

