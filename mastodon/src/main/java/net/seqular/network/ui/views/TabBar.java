package net.seqular.network.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import androidx.annotation.IdRes;

public class TabBar extends LinearLayout{
	@IdRes
	private int selectedTabID;
	private IntConsumer listener;
	private IntPredicate longClickListener;

	public TabBar(Context context){
		this(context, null);
	}

	public TabBar(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public TabBar(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}

	@Override
	public void onViewAdded(View child){
		super.onViewAdded(child);
		if(child.getId()!=0){
			if(selectedTabID==0){
				selectedTabID=child.getId();
				child.setSelected(true);
			}
			child.setOnClickListener(this::onChildClick);
			child.setOnLongClickListener(this::onChildLongClick);
		}
	}

	private void onChildClick(View v){
		listener.accept(v.getId());
		if(v.getId()==selectedTabID)
			return;
		selectTab(v.getId());
	}

	private boolean onChildLongClick(View v){
		return longClickListener.test(v.getId());
	}

	public void setListeners(IntConsumer listener, IntPredicate longClickListener){
		this.listener=listener;
		this.longClickListener=longClickListener;
	}

	public void selectTab(int id){
		toggleSelected(selectedTabID, false);
		selectedTabID=id;
		toggleSelected(id, true);
	}

	private void toggleSelected(int selectedTabID, boolean selected){
		LinearLayout tab=findViewById(selectedTabID);
		tab.setSelected(selected);
		View v=tab.findViewWithTag("label");
		if(v instanceof TextView text){
			text.setTypeface(Typeface.create(text.getTypeface(), selected ? Typeface.BOLD : Typeface.NORMAL));
		}
	}
}
