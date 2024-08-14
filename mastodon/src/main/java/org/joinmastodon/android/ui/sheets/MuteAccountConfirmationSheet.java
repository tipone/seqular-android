package org.joinmastodon.android.ui.sheets;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import org.joinmastodon.android.R;
import org.joinmastodon.android.model.Account;
import org.joinmastodon.android.ui.M3AlertDialogBuilder;
import org.joinmastodon.android.ui.views.M3Switch;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;

public class MuteAccountConfirmationSheet extends AccountRestrictionConfirmationSheet{
	public MuteAccountConfirmationSheet(@NonNull Context context, Account user, AtomicReference<Duration> muteDuration, AtomicBoolean muteNotifications, ConfirmCallback confirmCallback){
		super(context, user, confirmCallback);
		titleView.setText(R.string.mute_user_confirm_title);
		confirmBtn.setText(R.string.do_mute);
		secondaryBtn.setVisibility(View.GONE);
		icon.setImageResource(R.drawable.ic_fluent_speaker_off_24_regular);
		subtitleView.setText(user.getDisplayUsername());
		addRow(R.drawable.ic_campaign_24px, R.string.user_wont_know_muted);
		addRow(R.drawable.ic_fluent_eye_off_24_regular, R.string.user_can_still_see_your_posts);
		addRow(R.drawable.ic_fluent_mention_24_regular, R.string.you_wont_see_user_mentions);
		addRow(R.drawable.ic_fluent_arrow_reply_24_regular, R.string.user_can_mention_and_follow_you);

		// add mute notifications toggle (Moshidon)
		M3Switch m3Switch=new M3Switch(getContext());
		m3Switch.setClickable(true);
		m3Switch.setChecked(muteNotifications.get());
		m3Switch.setOnCheckedChangeListener((compoundButton, b) -> muteNotifications.set(b));
		m3Switch.setOnClickListener(view -> muteNotifications.set(m3Switch.isSelected()));
		addRow(R.drawable.ic_fluent_alert_off_24_regular, R.string.mo_mute_notifications, m3Switch);

		// add mute duration (Moshidon)
		Button muteDurationBtn=new Button(getContext());
		muteDurationBtn.setOnClickListener(v->getMuteDurationDialog(context, muteDuration, muteDurationBtn).show());
		muteDurationBtn.setText(R.string.sk_duration_indefinite);
		addRow(R.drawable.ic_fluent_clock_20_regular, R.string.sk_mute_label, muteDurationBtn);
	}

	@NonNull
	private M3AlertDialogBuilder getMuteDurationDialog(@NonNull Context context, AtomicReference<Duration> muteDuration, Button button){
		M3AlertDialogBuilder builder=new M3AlertDialogBuilder(context);
		builder.setTitle(R.string.sk_mute_label);
		builder.setIcon(R.drawable.ic_fluent_clock_20_regular);
		List<Duration> durations =List.of(Duration.ZERO,
				Duration.ofMinutes(5),
				Duration.ofMinutes(30),
				Duration.ofHours(1),
				Duration.ofHours(6),
				Duration.ofDays(1),
				Duration.ofDays(3),
				Duration.ofDays(7),
				Duration.ofDays(7));

		String[] choices = {context.getString(R.string.sk_duration_indefinite),
				context.getString(R.string.sk_duration_minutes_5),
				context.getString(R.string.sk_duration_minutes_30),
				context.getString(R.string.sk_duration_hours_1),
				context.getString(R.string.sk_duration_hours_6),
				context.getString(R.string.sk_duration_days_1),
				context.getString(R.string.sk_duration_days_3),
				context.getString(R.string.sk_duration_days_7)};

		builder.setSingleChoiceItems(choices, durations.indexOf(muteDuration.get()), (dialog, which) -> {});

		builder.setPositiveButton(R.string.ok, (dialog, which)->{
			int selected = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
			muteDuration.set(durations.get(selected));
			button.setText(choices[selected]);
		});
		builder.setNegativeButton(R.string.cancel, null);

		return builder;
	}


}
