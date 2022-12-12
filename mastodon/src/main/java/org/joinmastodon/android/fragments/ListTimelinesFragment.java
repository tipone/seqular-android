package org.joinmastodon.android.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.joinmastodon.android.R;
import org.joinmastodon.android.api.MastodonAPIRequest;
import org.joinmastodon.android.api.requests.lists.AddAccountsToList;
import org.joinmastodon.android.api.requests.lists.DeleteList;
import org.joinmastodon.android.api.requests.lists.GetLists;
import org.joinmastodon.android.api.requests.lists.RemoveAccountsFromList;
import org.joinmastodon.android.model.ListTimeline;
import org.joinmastodon.android.ui.M3AlertDialogBuilder;
import org.joinmastodon.android.ui.utils.UiUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.UsableRecyclerView;

public class ListTimelinesFragment extends BaseRecyclerFragment<ListTimeline> implements ScrollableToTop {
    private String accountId;
    private String profileAccountId;
    private String profileDisplayUsername;
    private HashMap<String, Boolean> userInListBefore = new HashMap<>();
    private HashMap<String, Boolean> userInList = new HashMap<>();
    private int inProgress = 0;

    public ListTimelinesFragment() {
        super(10);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args=getArguments();
        accountId=args.getString("account");

        if(args.containsKey("profileAccount")){
            profileAccountId=args.getString("profileAccount");
            profileDisplayUsername=args.getString("profileDisplayUsername");
            setTitle(getString(R.string.sk_lists_with_user, profileDisplayUsername));
//            setHasOptionsMenu(true);
        }
    }

    @Override
    protected void onShown(){
        super.onShown();
        if(!getArguments().getBoolean("noAutoLoad") && !loaded && !dataLoading)
            loadData();
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        Button saveButton=new Button(getActivity());
//        saveButton.setText(R.string.save);
//        saveButton.setOnClickListener(this::onSaveClick);
//        LinearLayout wrap=new LinearLayout(getActivity());
//        wrap.setOrientation(LinearLayout.HORIZONTAL);
//        wrap.addView(saveButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        wrap.setPadding(V.dp(16), V.dp(4), V.dp(16), V.dp(8));
//        wrap.setClipToPadding(false);
//        MenuItem item=menu.add(R.string.save);
//        item.setActionView(wrap);
//        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//    }

    private void saveListMembership(String listId, boolean isMember) {
        userInList.put(listId, isMember);
        List<String> accountIdList = Collections.singletonList(profileAccountId);
        MastodonAPIRequest<Object> req = isMember ? new AddAccountsToList(listId, accountIdList) : new RemoveAccountsFromList(listId, accountIdList);
        req.setCallback(new SimpleCallback<>(this) {
            @Override
            public void onSuccess(Object o) {}
        }).exec(accountId);
    }

    @Override
    protected void doLoadData(int offset, int count){
        userInListBefore.clear();
        userInList.clear();
        currentRequest=(profileAccountId != null ? new GetLists(profileAccountId) : new GetLists())
                .setCallback(new SimpleCallback<>(this) {
                    @Override
                    public void onSuccess(List<ListTimeline> lists) {
                        for (ListTimeline l : lists) userInListBefore.put(l.id, true);
                        userInList.putAll(userInListBefore);
                        if (profileAccountId == null || !lists.isEmpty()) onDataLoaded(lists, false);
                        if (profileAccountId == null) return;

                        currentRequest=new GetLists().setCallback(new SimpleCallback<>(ListTimelinesFragment.this) {
                            @Override
                            public void onSuccess(List<ListTimeline> allLists) {
                                List<ListTimeline> newLists = new ArrayList<>();
                                for (ListTimeline l : allLists) {
                                    if (lists.stream().noneMatch(e -> e.id.equals(l.id))) newLists.add(l);
                                    if (!userInListBefore.containsKey(l.id)) {
                                        userInListBefore.put(l.id, false);
                                    }
                                }
                                userInList.putAll(userInListBefore);
                                onDataLoaded(newLists, false);
                            }
                        }).exec(accountId);
                    }
                })
                .exec(accountId);
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return new ListsAdapter();
    }

    @Override
    public void scrollToTop() {
        smoothScrollRecyclerViewToTop(list);
    }

    private class ListsAdapter extends RecyclerView.Adapter<ListViewHolder>{
        @NonNull
        @Override
        public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            return new ListViewHolder();
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class ListViewHolder extends BindableViewHolder<ListTimeline> implements UsableRecyclerView.Clickable{
        private final TextView title;
        private final CheckBox listToggle;
        private final ImageView edit, delete;

        public ListViewHolder(){
            super(getActivity(), R.layout.item_list_timeline, list);
            title=findViewById(R.id.title);
            listToggle=findViewById(R.id.list_toggle);
            edit=findViewById(R.id.edit);
            delete=findViewById(R.id.delete);
        }

        @Override
        public void onBind(ListTimeline item) {
            title.setText(item.title);
            if (profileAccountId != null) {
                Boolean checked = userInList.get(item.id);
                listToggle.setChecked(userInList.containsKey(item.id) && checked != null && checked);
                listToggle.setOnClickListener(this::onClickToggle);
            } else {
                listToggle.setVisibility(View.GONE);
            }
            edit.setOnClickListener(v -> this.editListName(item.id));
            delete.setOnClickListener(v -> this.deleteList(item.id));
        }

        private void onClickToggle(View view) {
            saveListMembership(item.id, listToggle.isChecked());
        }

        @Override
        public void onClick() {
            UiUtils.openListTimeline(getActivity(), accountId, item);
        }

        private void editListName(String listId){
            new M3AlertDialogBuilder(getActivity())
                    .setTitle(R.string.edit_text_edited)
//                    .setPositiveButton(R.string.discard, (dialog, which)-> )
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        private void deleteList(String listId){
            new M3AlertDialogBuilder(getActivity())
                    .setTitle(R.string.sk_delete_list_dialog_title)
                    .setPositiveButton(R.string.delete, (dialog, which)-> new DeleteList(listId))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        private void actuallyDeleteList(String listId){
            new DeleteList(listId).setCallback(new Callback<Object>() {

                @Override
                public void onSuccess(Object result) {
                    loadData();
                }

                @Override
                public void onError(ErrorResponse error) {
                    error.showToast(getActivity());
                }
            });
        }
    }
}
