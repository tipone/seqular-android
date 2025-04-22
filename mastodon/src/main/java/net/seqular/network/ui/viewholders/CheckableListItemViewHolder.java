package net.seqular.network.ui.viewholders;

import android.content.Context;
import android.view.ViewGroup;

import net.seqular.network.R;
import net.seqular.network.model.viewmodel.CheckableListItem;
import net.seqular.network.ui.views.CheckableLinearLayout;

public abstract class CheckableListItemViewHolder extends ListItemViewHolder<CheckableListItem<?>>{
	protected final CheckableLinearLayout checkableLayout;

	public CheckableListItemViewHolder(Context context, ViewGroup parent){
		super(context, R.layout.item_generic_list_checkable, parent);
		checkableLayout=(CheckableLinearLayout) itemView;
	}

	@Override
	public void onBind(CheckableListItem<?> item){
		super.onBind(item);
		checkableLayout.setChecked(item.checked);
	}
}
