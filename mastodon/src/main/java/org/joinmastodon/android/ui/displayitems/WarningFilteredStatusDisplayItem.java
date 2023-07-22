package org.joinmastodon.android.ui.displayitems;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.model.AltTextFilter;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.Status;

import java.util.List;

// Mind the gap!
public class WarningFilteredStatusDisplayItem extends StatusDisplayItem{
	public boolean loading;
	public final Status status;
	public List<StatusDisplayItem> filteredItems;
	public Filter applyingFilter;

	public WarningFilteredStatusDisplayItem(String parentID, BaseStatusListFragment<?> parentFragment, Status status, List<StatusDisplayItem> filteredItems, Filter applyingFilter){
		super(parentID, parentFragment);
		this.status=status;
		this.filteredItems = filteredItems;
		this.applyingFilter = applyingFilter;
	}

	@Override
	public Type getType(){
		return Type.WARNING;
	}

    public static class Holder extends StatusDisplayItem.Holder<WarningFilteredStatusDisplayItem>{
        public final View warningWrap;
        public final Button showBtn;
        public final TextView text;
        public List<StatusDisplayItem> filteredItems;

        public Holder(Context context, ViewGroup parent){
            super(context, R.layout.display_item_warning, parent);
            warningWrap=findViewById(R.id.warning_wrap);
            showBtn=findViewById(R.id.reveal_btn);
            showBtn.setOnClickListener(i -> item.parentFragment.onWarningClick(this));
            itemView.setOnClickListener(v->item.parentFragment.onWarningClick(this));
            text=findViewById(R.id.text);
        }

		@Override
		public void onBind(WarningFilteredStatusDisplayItem item) {
			filteredItems = item.filteredItems;
			if(item.applyingFilter instanceof AltTextFilter){
				text.setText(item.parentFragment.getString(R.string.sk_filtered,item.parentFragment.getString(R.string.sk_no_alt_text)));
			}else{
				text.setText(item.parentFragment.getString(R.string.sk_filtered, item.applyingFilter.title));
			}
		}

        @Override
        public void onClick(){

        }
    }
}
