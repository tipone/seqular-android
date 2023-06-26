package org.joinmastodon.android.fragments.settings;

import android.os.Build;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.SettingsFragment;
import org.joinmastodon.android.ui.M3AlertDialogBuilder;

import java.util.ArrayList;

import me.grishka.appkit.utils.V;

public class BehaviourFragment extends SettingsBaseFragment{

    SwitchItem alwaysRevealSpoilersItem;
    ButtonItem autoRevealSpoilersItem;
    ButtonItem publishButtonTextSetting;
    SwitchItem relocatePublishButtonSetting;
    @Override
    public void addItems(ArrayList<Item> items) {
        items.add(new HeaderItem(R.string.settings_behavior));
        items.add(new SwitchItem(R.string.settings_gif, R.string.mo_setting_play_gif_summary, R.drawable.ic_fluent_gif_24_regular, GlobalUserPreferences.playGifs, i->{
            GlobalUserPreferences.playGifs=i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(new SwitchItem(R.string.settings_custom_tabs, R.drawable.ic_fluent_link_24_regular, GlobalUserPreferences.useCustomTabs, i->{
            GlobalUserPreferences.useCustomTabs=i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(new SettingsBaseFragment.SwitchItem(R.string.sk_settings_allow_remote_loading, R.string.sk_settings_allow_remote_loading_explanation,  R.drawable.ic_fluent_communication_24_regular, GlobalUserPreferences.allowRemoteLoading, i->{
            GlobalUserPreferences.allowRemoteLoading=i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(alwaysRevealSpoilersItem = new SettingsBaseFragment.SwitchItem(R.string.sk_settings_always_reveal_content_warnings, R.drawable.ic_fluent_chat_warning_24_regular, GlobalUserPreferences.alwaysExpandContentWarnings, i->{
            GlobalUserPreferences.alwaysExpandContentWarnings=i.checked;
            GlobalUserPreferences.save();
            if (list.findViewHolderForAdapterPosition(items.indexOf(autoRevealSpoilersItem)) instanceof SettingsBaseFragment.ButtonViewHolder bvh) bvh.rebind();
        }));
        items.add(autoRevealSpoilersItem = new SettingsBaseFragment.ButtonItem(R.string.sk_settings_auto_reveal_equal_spoilers, R.drawable.ic_fluent_eye_24_regular, b->{
            PopupMenu popupMenu=new PopupMenu(getActivity(), b, Gravity.CENTER_HORIZONTAL);
            popupMenu.inflate(R.menu.settings_auto_reveal_spoiler);
            popupMenu.setOnMenuItemClickListener(i -> onAutoRevealSpoilerClick(i, b));
            b.setOnTouchListener(popupMenu.getDragToOpenListener());
            b.setOnClickListener(v->popupMenu.show());
            onAutoRevealSpoilerChanged(b);
        }));

        items.add(new SwitchItem(R.string.sk_tabs_disable_swipe, R.string.mo_setting_disable_swipe_summary, R.drawable.ic_fluent_swipe_right_24_regular, GlobalUserPreferences.disableSwipe, i->{
            GlobalUserPreferences.disableSwipe=i.checked;
            GlobalUserPreferences.save();
            needAppRestart=true;
        }));
        items.add(new SwitchItem(R.string.mo_double_tap_to_swipe_between_tabs, R.drawable.ic_fluent_double_tap_swipe_right_24_regular, GlobalUserPreferences.doubleTapToSwipe, i->{
            GlobalUserPreferences.doubleTapToSwipe=i.checked;
            GlobalUserPreferences.save();
            needAppRestart=true;
        }));
        items.add(new SwitchItem(R.string.mo_haptic_feedback, R.string.mo_setting_haptic_feedback_summary, R.drawable.ic_fluent_phone_vibrate_24_filled, GlobalUserPreferences.hapticFeedback, i -> {
            GlobalUserPreferences.hapticFeedback = i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(new SwitchItem(R.string.sk_settings_confirm_before_reblog, R.drawable.ic_fluent_checkmark_circle_24_regular, GlobalUserPreferences.confirmBeforeReblog, i->{
            GlobalUserPreferences.confirmBeforeReblog=i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(new SettingsBaseFragment.SwitchItem(R.string.sk_settings_forward_report_default, R.drawable.ic_fluent_arrow_forward_24_regular, GlobalUserPreferences.forwardReportDefault, i->{
            GlobalUserPreferences.forwardReportDefault=i.checked;
            GlobalUserPreferences.save();
        }));

        items.add(new HeaderItem(R.string.mo_composer_behavior));
        items.add(publishButtonTextSetting = new ButtonItem(R.string.sk_settings_publish_button_text, R.drawable.ic_fluent_send_24_regular, b-> {
            updatePublishText(b);
            b.setOnClickListener(l -> {
                if(!GlobalUserPreferences.relocatePublishButton) {
                    FrameLayout inputWrap = new FrameLayout(getContext());
                    EditText input = new EditText(getContext());
                    input.setHint(R.string.publish);
                    input.setText(GlobalUserPreferences.publishButtonText.trim());
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(V.dp(16), V.dp(4), V.dp(16), V.dp(16));
                    input.setLayoutParams(params);
                    inputWrap.addView(input);
                    new M3AlertDialogBuilder(getContext()).setTitle(R.string.sk_settings_publish_button_text_title).setView(inputWrap)
                            .setPositiveButton(R.string.save, (d, which) -> {
                                GlobalUserPreferences.publishButtonText = input.getText().toString().trim();
                                GlobalUserPreferences.save();
                                updatePublishText(b);
                            })
                            .setNeutralButton(R.string.clear, (d, which) -> {
                                GlobalUserPreferences.publishButtonText = "";
                                GlobalUserPreferences.save();
                                updatePublishText(b);
                            })
                            .setNegativeButton(R.string.cancel, (d, which) -> {
                            })
                            .show();

                } else {
                    Toast.makeText(getActivity(), R.string.mo_disable_relocate_publish_button_to_enable_customization,
                            Toast.LENGTH_LONG).show();
                }
            });

            b.setAlpha(relocatePublishButtonSetting.checked ? 0.7f : 1f);
        }));
        items.add(relocatePublishButtonSetting = new SwitchItem(R.string.mo_relocate_publish_button, R.string.mo_setting_relocate_publish_summary, R.drawable.ic_fluent_arrow_autofit_down_24_regular, GlobalUserPreferences.relocatePublishButton, i->{
            if (list.findViewHolderForAdapterPosition(items.indexOf(publishButtonTextSetting)) instanceof SettingsBaseFragment.ButtonViewHolder bvh) bvh.rebind();
            GlobalUserPreferences.relocatePublishButton=i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(new SwitchItem(R.string.mo_change_default_reply_visibility_to_unlisted, R.string.mo_setting_default_reply_privacy_summary, R.drawable.ic_fluent_lock_open_24_regular, GlobalUserPreferences.defaultToUnlistedReplies, i->{
            GlobalUserPreferences.defaultToUnlistedReplies=i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(new SwitchItem(R.string.mo_mention_reblogger_automatically, R.drawable.ic_fluent_comment_mention_24_regular, GlobalUserPreferences.mentionRebloggerAutomatically, i -> {
            GlobalUserPreferences.mentionRebloggerAutomatically=i.checked;
            GlobalUserPreferences.save();
        }));
        items.add(new SwitchItem(R.string.mo_disable_reminder_to_add_alt_text, R.drawable.ic_fluent_image_alt_text_24_regular, GlobalUserPreferences.disableAltTextReminder, i->{
            GlobalUserPreferences.disableAltTextReminder=i.checked;
            GlobalUserPreferences.save();
            needAppRestart=true;
        }));
        items.add(new SettingsBaseFragment.ButtonItem(R.string.sk_settings_prefix_reply_cw_with_re, R.drawable.ic_fluent_arrow_reply_24_regular, b->{
            PopupMenu popupMenu=new PopupMenu(getActivity(), b, Gravity.CENTER_HORIZONTAL);
            popupMenu.inflate(R.menu.settings_prefix_reply_mode);
            popupMenu.setOnMenuItemClickListener(i -> onPrefixRepliesClick(i, b));
            b.setOnTouchListener(popupMenu.getDragToOpenListener());
            b.setOnClickListener(v->popupMenu.show());
            b.setText(switch(GlobalUserPreferences.prefixReplies){
                case TO_OTHERS -> R.string.sk_settings_prefix_replies_to_others;
                case ALWAYS -> R.string.sk_settings_prefix_replies_always;
                default -> R.string.sk_settings_prefix_replies_never;
            });
            GlobalUserPreferences.save();
        }));
    }

    private boolean onPrefixRepliesClick(MenuItem item, Button btn) {
        int id = item.getItemId();
        GlobalUserPreferences.PrefixRepliesMode mode = GlobalUserPreferences.PrefixRepliesMode.NEVER;
        if (id == R.id.prefix_replies_always) mode = GlobalUserPreferences.PrefixRepliesMode.ALWAYS;
        else if (id == R.id.prefix_replies_to_others) mode = GlobalUserPreferences.PrefixRepliesMode.TO_OTHERS;
        GlobalUserPreferences.prefixReplies = mode;

        btn.setText(switch(GlobalUserPreferences.prefixReplies){
            case TO_OTHERS -> R.string.sk_settings_prefix_replies_to_others;
            case ALWAYS -> R.string.sk_settings_prefix_replies_always;
            default -> R.string.sk_settings_prefix_replies_never;
        });

        return true;
    }

    private boolean onAutoRevealSpoilerClick(MenuItem item, Button btn) {
        int id = item.getItemId();

        GlobalUserPreferences.AutoRevealMode mode = GlobalUserPreferences.AutoRevealMode.NEVER;
        if (id == R.id.auto_reveal_threads) mode = GlobalUserPreferences.AutoRevealMode.THREADS;
        else if (id == R.id.auto_reveal_discussions) mode = GlobalUserPreferences.AutoRevealMode.DISCUSSIONS;

        GlobalUserPreferences.alwaysExpandContentWarnings = false;
        GlobalUserPreferences.autoRevealEqualSpoilers = mode;
        GlobalUserPreferences.save();
        onAutoRevealSpoilerChanged(btn);
        return true;
    }

    private void onAutoRevealSpoilerChanged(Button b) {
        if (GlobalUserPreferences.alwaysExpandContentWarnings) {
            b.setText(R.string.sk_settings_auto_reveal_anyone);
        } else {
            b.setText(switch(GlobalUserPreferences.autoRevealEqualSpoilers){
                case THREADS -> R.string.sk_settings_auto_reveal_author;
                case DISCUSSIONS -> R.string.sk_settings_auto_reveal_anyone;
                default -> R.string.sk_settings_auto_reveal_nobody;
            });
            if (alwaysRevealSpoilersItem.checked != GlobalUserPreferences.alwaysExpandContentWarnings) {
                alwaysRevealSpoilersItem.checked = GlobalUserPreferences.alwaysExpandContentWarnings;
                if (list.findViewHolderForAdapterPosition(items.indexOf(alwaysRevealSpoilersItem)) instanceof SettingsBaseFragment.SwitchViewHolder svh) svh.rebind();
            }
        }
    }

    private void updatePublishText(Button btn) {
        if (GlobalUserPreferences.publishButtonText.isBlank()) btn.setText(R.string.publish);
        else btn.setText(GlobalUserPreferences.publishButtonText);
    }
}
