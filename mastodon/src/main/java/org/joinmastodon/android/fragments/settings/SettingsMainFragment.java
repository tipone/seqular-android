package org.joinmastodon.android.fragments.settings;

import android.os.Bundle;
import android.view.View;

import org.joinmastodon.android.E;
import org.joinmastodon.android.R;
import org.joinmastodon.android.updater.GithubSelfUpdater;

import java.util.ArrayList;

public class SettingsMainFragment extends SettingsBaseFragment {
    @Override
    public void addItems(ArrayList<Item> items) {

        if (GithubSelfUpdater.needSelfUpdating()) {
            GithubSelfUpdater updater = GithubSelfUpdater.getInstance();
            GithubSelfUpdater.UpdateState state = updater.getState();
            if (state != GithubSelfUpdater.UpdateState.NO_UPDATE && state != GithubSelfUpdater.UpdateState.CHECKING && updater.getUpdateInfo() != null) {
                items.add(new SettingsBaseFragment.UpdateItem());
            }
        }

        items.add(new SettingsCategoryItem(R.string.settings_theme, AppearanceFragment.class, R.drawable.ic_fluent_color_24_regular));
        items.add(new SettingsCategoryItem(R.string.settings_behavior, BehaviourFragment.class, R.drawable.ic_fluent_chat_settings_24_regular));
        items.add(new SettingsCategoryItem(R.string.sk_timelines, TimeLineFragment.class, R.drawable.ic_fluent_timeline_24_regular));
        items.add(new SettingsCategoryItem(R.string.settings_notifications, NotificationsFragment.class, R.drawable.ic_fluent_alert_28_regular_badged));
        items.add(new SettingsCategoryItem(R.string.settings_account, AccountFragment.class, R.drawable.ic_fluent_person_28_regular));
        items.add(new SettingsCategoryItem(R.string.sk_settings_about, AboutFragment.class, R.drawable.ic_fluent_info_24_regular));

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
