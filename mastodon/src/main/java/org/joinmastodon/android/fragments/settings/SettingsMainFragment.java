package org.joinmastodon.android.fragments.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.joinmastodon.android.E;
import org.joinmastodon.android.R;
import org.joinmastodon.android.events.SelfUpdateStateChangedEvent;
import org.joinmastodon.android.updater.GithubSelfUpdater;

import java.util.ArrayList;

public class SettingsMainFragment extends SettingsBaseFragment {

    protected UpdateItem updateItem;
    @Override
    public void addItems(ArrayList<Item> items) {

        if (GithubSelfUpdater.needSelfUpdating()) {
            GithubSelfUpdater updater = GithubSelfUpdater.getInstance();
            GithubSelfUpdater.UpdateState state = updater.getState();
            if (state != GithubSelfUpdater.UpdateState.NO_UPDATE && state != GithubSelfUpdater.UpdateState.CHECKING && updater.getUpdateInfo() != null) {
                items.add(updateItem = new SettingsBaseFragment.UpdateItem());
            }
        }

        items.add(new SettingsCategoryItem(R.string.settings_theme, AppearanceFragment.class, R.drawable.ic_fluent_paint_brush_24_regular));
        items.add(new SettingsCategoryItem(R.string.settings_behavior, BehaviourFragment.class, R.drawable.ic_fluent_chat_settings_24_regular));
        items.add(new SettingsCategoryItem(R.string.sk_timelines, TimeLineFragment.class, R.drawable.ic_fluent_timeline_24_regular));
        items.add(new SettingsCategoryItem(R.string.settings_notifications, NotificationsFragment.class, R.drawable.ic_fluent_alert_28_regular_badged));
        items.add(new SettingsCategoryItem(R.string.settings_account, AccountFragment.class, R.drawable.ic_fluent_person_28_regular));
        items.add(new SettingsCategoryItem(R.string.sk_settings_about, AboutFragment.class, R.drawable.ic_fluent_info_24_regular));

    }

    @Subscribe
    public void onSelfUpdateStateChanged(SelfUpdateStateChangedEvent ev){
//        checkForUpdateItem.loading = ev.state == GithubSelfUpdater.UpdateState.CHECKING;
//        if (list.findViewHolderForAdapterPosition(items.indexOf(checkForUpdateItem)) instanceof TextViewHolder tvh) tvh.rebind();

        if (ev.state != GithubSelfUpdater.UpdateState.CHECKING
                && ev.state != GithubSelfUpdater.UpdateState.NO_UPDATE) {
            updateItem = new UpdateItem();
            items.remove(1);
            items.add(1, updateItem);
            list.setAdapter(new SettingsAdapter());
        }

        if(updateItem != null && list.findViewHolderForAdapterPosition(0) instanceof SettingsBaseFragment.UpdateViewHolder uvh){
            uvh.bind(updateItem);
        }

        if (ev.state == GithubSelfUpdater.UpdateState.NO_UPDATE) {
            Toast.makeText(getActivity(), R.string.sk_no_update_available, Toast.LENGTH_SHORT).show();
        } else if (ev.state == GithubSelfUpdater.UpdateState.UPDATE_AVAILABLE){
            Toast.makeText(getActivity(), getString(R.string.mo_update_available, GithubSelfUpdater.getInstance().getUpdateInfo().version), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (GithubSelfUpdater.needSelfUpdating()) {
            E.register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (GithubSelfUpdater.needSelfUpdating())
            E.unregister(this);
    }
}
