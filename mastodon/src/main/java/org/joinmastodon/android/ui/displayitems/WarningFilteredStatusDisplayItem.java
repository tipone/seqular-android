package org.joinmastodon.android.ui.displayitems;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.model.Status;
import org.joinmastodon.android.ui.drawables.SawtoothTearDrawable;

import java.util.ArrayList;

// Mind the gap!
public class WarningFilteredStatusDisplayItem extends StatusDisplayItem{
    public boolean loading;
    public final Status status;
    public ArrayList<StatusDisplayItem> filteredItems;

    public WarningFilteredStatusDisplayItem(String parentID, BaseStatusListFragment parentFragment, Status status, ArrayList<StatusDisplayItem> items){
        super(parentID, parentFragment);
        this.status=status;
        this.filteredItems = items;
    }

    @Override
    public Type getType(){
        return Type.WARNING;
    }

    public static class Holder extends StatusDisplayItem.Holder<WarningFilteredStatusDisplayItem>{
        public final View warningWrap;
        public final ProgressBar progress;
        public final TextView text;
        public ArrayList<StatusDisplayItem> filteredItems;

        public Holder(Context context, ViewGroup parent){
            super(context, R.layout.display_item_warning, parent);
            warningWrap=findViewById(R.id.warning_wrap);
            progress=findViewById(R.id.progress);
            text=findViewById(R.id.text);
//            itemView.setOnClickListener(v->item.parentFragment.onRevealFilteredClick(this));
        }

        @Override
        public void onBind(WarningFilteredStatusDisplayItem item){
            filteredItems = item.filteredItems;
        }

        @Override
        public void onClick(){
//            this.itemView.setVisibility(View.GONE);
//            ViewGroup.LayoutParams params = this.itemView.getLayoutParams();
//            params.height = 0;
//            params.width = 0;
//            this.itemView.setLayoutParams(params);
            item.parentFragment.onWarningClick(this);
        }
    }
}
