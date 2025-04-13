package net.seqular.network.fragments;

import android.view.ViewTreeObserver;

import androidx.recyclerview.widget.RecyclerView;

import net.seqular.network.ui.utils.UiUtils;

public interface ScrollableToTop{
//	boolean isScrolledToTop();

	void scrollToTop();

	/**
	 * Utility method to scroll a RecyclerView to top in a way that doesn't suck
	 * @param list
	 */
	default void smoothScrollRecyclerViewToTop(RecyclerView list){
		if(list==null) // TODO find out why this happens because it should not be possible
			return;
		if(list.getChildCount()>0 && list.getChildAdapterPosition(list.getChildAt(0))>10){
			list.scrollToPosition(0);
			list.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
				@Override
				public boolean onPreDraw(){
					list.getViewTreeObserver().removeOnPreDrawListener(this);
					list.scrollBy(0, UiUtils.SCROLL_TO_TOP_DELTA);
					list.smoothScrollToPosition(0);
					return true;
				}
			});
		}else{
			list.smoothScrollToPosition(0);
		}
	}
}
