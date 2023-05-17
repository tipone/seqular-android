package org.joinmastodon.android.fragments.settings;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import org.joinmastodon.android.R;

import java.util.ArrayList;

public class SettingsMainFragment extends SettingsBaseFragment{
    @Override
    public void addItems(ArrayList<Item> items) {
        items.add(new GiantHeaderItem(getContext().getString(R.string.settings)));
        items.add(new SettingsCategoryItem(R.string.settings_theme, SettingsAppearanceFragment.class, R.drawable.ic_fluent_color_24_regular));
        items.add(new SettingsCategoryItem(R.string.settings_behavior, BehaviourFragment.class, R.drawable.ic_fluent_chat_settings_24_regular));
        items.add(new SettingsCategoryItem(R.string.sk_timelines, TimeLineFragment.class, R.drawable.ic_fluent_timeline_24_regular));
        items.add(new SettingsCategoryItem(R.string.settings_notifications, NotificationsFragment.class, R.drawable.ic_fluent_alert_28_regular_badged));
        items.add(new SettingsCategoryItem(R.string.settings_account, AccountFragment.class, R.drawable.ic_fluent_person_28_regular));
        items.add(new SettingsCategoryItem(R.string.sk_settings_about, AboutFragment.class, R.drawable.ic_fluent_info_24_regular));

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideToolbar();
    }
}
