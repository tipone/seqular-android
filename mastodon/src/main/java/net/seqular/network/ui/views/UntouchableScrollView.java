package net.seqular.network.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class UntouchableScrollView extends ScrollView {
	public UntouchableScrollView(Context context) {
		super(context);
	}

	public UntouchableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UntouchableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public UntouchableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return false;
	}
}
