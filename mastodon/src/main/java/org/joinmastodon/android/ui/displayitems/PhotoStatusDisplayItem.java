package org.joinmastodon.android.ui.displayitems;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.model.Attachment;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.PhotoLayoutHelper;

import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.CubicBezierInterpolator;
import me.grishka.appkit.utils.V;

public class PhotoStatusDisplayItem extends ImageStatusDisplayItem{
	public PhotoStatusDisplayItem(String parentID, Status status, Attachment photo, BaseStatusListFragment parentFragment, int index, int totalPhotos, PhotoLayoutHelper.TiledLayoutResult tiledLayout, PhotoLayoutHelper.TiledLayoutResult.Tile thisTile){
		super(parentID, parentFragment, photo, status, index, totalPhotos, tiledLayout, thisTile);
		request=new UrlImageLoaderRequest(photo.url, 1000, 1000);
	}

	@Override
	public Type getType(){
		return Type.PHOTO;
	}

	public static class Holder extends ImageStatusDisplayItem.Holder<PhotoStatusDisplayItem> {
		public Holder(Activity activity, ViewGroup parent) {
			super(activity, R.layout.display_item_photo, parent);
		}
	}
}
