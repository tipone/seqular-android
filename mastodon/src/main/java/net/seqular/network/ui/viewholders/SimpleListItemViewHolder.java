package net.seqular.network.ui.viewholders;

import android.content.Context;
import android.view.ViewGroup;

import net.seqular.network.R;
import net.seqular.network.model.viewmodel.ListItem;

public class SimpleListItemViewHolder extends ListItemViewHolder<ListItem<?>>{
	public SimpleListItemViewHolder(Context context, ViewGroup parent){
		super(context, R.layout.item_generic_list, parent);
	}
}
