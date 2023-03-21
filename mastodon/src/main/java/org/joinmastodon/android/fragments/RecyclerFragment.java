package org.joinmastodon.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.joinmastodon.android.R;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.grishka.appkit.fragments.BaseRecyclerFragment;


public abstract class RecyclerFragment<T> extends BaseRecyclerFragment<T> {
	public RecyclerFragment(int perPage) {
		super(perPage);
	}

	public RecyclerFragment(int layout, int perPage) {
		super(layout, perPage);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (refreshLayout != null) setRefreshLayoutColors(refreshLayout);
	}

	public static void setRefreshLayoutColors(SwipeRefreshLayout l) {
		List<Integer> colors = new ArrayList<>(Arrays.asList(
				R.color.primary_600,
				R.color.red_primary_600,
				R.color.green_primary_600,
				R.color.blue_primary_600,
				R.color.purple_600
		));
		int primary = UiUtils.getThemeColorRes(l.getContext(), R.attr.colorPrimary600);
		if (!colors.contains(primary)) colors.add(0, primary);
		int offset = colors.indexOf(primary);
		int[] sorted = new int[colors.size()];
		for (int i = 0; i < colors.size(); i++) {
			sorted[i] = colors.get((i + offset) % colors.size());
		}
		l.setColorSchemeResources(sorted);
	}
}
