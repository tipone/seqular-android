package net.seqular.network.fragments.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.StringRes;

import net.seqular.network.E;
import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.MastodonApp;
import net.seqular.network.R;
import net.seqular.network.api.session.AccountLocalPreferences;
import net.seqular.network.api.session.AccountLocalPreferences.ColorPreference;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.events.StatusDisplaySettingsChangedEvent;
import net.seqular.network.model.viewmodel.CheckableListItem;
import net.seqular.network.model.viewmodel.ListItem;
import net.seqular.network.ui.M3AlertDialogBuilder;
import net.seqular.network.ui.views.TextInputFrameLayout;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import me.grishka.appkit.FragmentStackActivity;

public class SettingsDisplayFragment extends BaseSettingsFragment<Void>{
	private ImageView themeTransitionWindowView;
	private ListItem<Void> themeItem;
	private CheckableListItem<Void> revealCWsItem, hideSensitiveMediaItem, interactionCountsItem, emojiInNamesItem;

	// MEGALODON
	private CheckableListItem<Void> trueBlackModeItem, marqueeItem, disableSwipeItem, reduceMotionItem, altIndicatorItem, noAltIndicatorItem, collapsePostsItem, spectatorModeItem, hideFabItem, translateOpenedItem, disablePillItem, showNavigationLabelsItem, likeIconItem, underlinedLinksItem;
	private ListItem<Void> colorItem, publishTextItem, autoRevealCWsItem;
	private CheckableListItem<Void> pronounsInUserListingsItem, pronounsInTimelinesItem, pronounsInThreadsItem;

	// MOSHIDON
	private  CheckableListItem<Void> enableDoubleTapToSwipeItem, relocatePublishButtonItem, showPostDividersItem, enableDoubleTapToSearchItem, showMediaPreviewItem, enhanceTextSizeItem;

	private AccountLocalPreferences lp;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(R.string.settings_display);
		AccountSession s=AccountSessionManager.get(accountID);
		lp=s.getLocalPreferences();
		onDataLoaded(List.of(
				themeItem=new ListItem<>(R.string.settings_theme, getAppearanceValue(), R.drawable.ic_fluent_weather_moon_24_regular, this::onAppearanceClick),
				colorItem=new ListItem<>(getString(R.string.sk_settings_color_palette), getColorPaletteValue(), R.drawable.ic_fluent_color_24_regular, this::onColorClick),
				trueBlackModeItem=new CheckableListItem<>(R.string.sk_settings_true_black, R.string.mo_setting_true_black_summary, CheckableListItem.Style.SWITCH, GlobalUserPreferences.trueBlackTheme, R.drawable.ic_fluent_dark_theme_24_regular, i->onTrueBlackModeClick(), true),
				publishTextItem=new ListItem<>(getString(R.string.sk_settings_publish_button_text), getPublishButtonText(), R.drawable.ic_fluent_send_24_regular, this::onPublishTextClick),
				autoRevealCWsItem=new ListItem<>(R.string.sk_settings_auto_reveal_equal_spoilers, getAutoRevealSpoilersText(), R.drawable.ic_fluent_eye_24_regular, this::onAutoRevealSpoilersClick),
				enhanceTextSizeItem=new CheckableListItem<>(R.string.mo_settings_enhance_text_size, R.string.mo_settings_enhance_text_size_summary, CheckableListItem.Style.SWITCH, GlobalUserPreferences.enhanceTextSize, R.drawable.ic_fluent_text_more_24_regular, i->onEnhanceTextSizeClick()),
				relocatePublishButtonItem=new CheckableListItem<>(R.string.mo_relocate_publish_button, R.string.mo_setting_relocate_publish_summary, CheckableListItem.Style.SWITCH, GlobalUserPreferences.relocatePublishButton, R.drawable.ic_fluent_arrow_autofit_down_24_regular, i->toggleCheckableItem(relocatePublishButtonItem)),
				revealCWsItem=new CheckableListItem<>(R.string.sk_settings_always_reveal_content_warnings, 0, CheckableListItem.Style.SWITCH, lp.revealCWs, R.drawable.ic_fluent_chat_warning_24_regular, i->toggleCheckableItem(revealCWsItem)),
				hideSensitiveMediaItem=new CheckableListItem<>(R.string.settings_hide_sensitive_media, 0, CheckableListItem.Style.SWITCH, lp.hideSensitiveMedia, R.drawable.ic_fluent_flag_24_regular, i->toggleCheckableItem(hideSensitiveMediaItem)),
				showMediaPreviewItem=new CheckableListItem<>(R.string.mo_show_media_preview, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.showMediaPreview, R.drawable.ic_fluent_image_24_regular, i->toggleCheckableItem(showMediaPreviewItem)),
				interactionCountsItem=new CheckableListItem<>(R.string.settings_show_interaction_counts, R.string.mo_setting_interaction_count_summary, CheckableListItem.Style.SWITCH, lp.showInteractionCounts, R.drawable.ic_fluent_number_row_24_regular, i->toggleCheckableItem(interactionCountsItem)),
				emojiInNamesItem=new CheckableListItem<>(R.string.settings_show_emoji_in_names, 0, CheckableListItem.Style.SWITCH, lp.customEmojiInNames, R.drawable.ic_fluent_emoji_24_regular, i->toggleCheckableItem(emojiInNamesItem)),
				marqueeItem=new CheckableListItem<>(R.string.sk_settings_enable_marquee, R.string.mo_setting_marquee_summary, CheckableListItem.Style.SWITCH, GlobalUserPreferences.toolbarMarquee, R.drawable.ic_fluent_text_more_24_regular, i->toggleCheckableItem(marqueeItem)),
				reduceMotionItem=new CheckableListItem<>(R.string.sk_settings_reduce_motion, R.string.mo_setting_reduced_motion_summary, CheckableListItem.Style.SWITCH, GlobalUserPreferences.reduceMotion, R.drawable.ic_fluent_star_emphasis_24_regular, i->toggleCheckableItem(reduceMotionItem)),
				enableDoubleTapToSearchItem=new CheckableListItem<>(R.string.mo_double_tap_to_search, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.doubleTapToSearch, R.drawable.ic_fluent_search_24_regular, i->toggleCheckableItem(enableDoubleTapToSearchItem)),
				disableSwipeItem=new CheckableListItem<>(R.string.sk_settings_tabs_disable_swipe, R.string.mo_setting_disable_swipe_summary, CheckableListItem.Style.SWITCH, GlobalUserPreferences.disableSwipe, R.drawable.ic_fluent_swipe_right_24_regular, i->toggleCheckableItem(disableSwipeItem)),
				enableDoubleTapToSwipeItem=new CheckableListItem<>(R.string.mo_double_tap_to_swipe_between_tabs, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.doubleTapToSwipe, R.drawable.ic_fluent_double_tap_swipe_right_24_regular, i->toggleCheckableItem(enableDoubleTapToSwipeItem)),
				altIndicatorItem=new CheckableListItem<>(R.string.sk_settings_show_alt_indicator, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.showAltIndicator, R.drawable.ic_fluent_scan_text_24_regular, i->toggleCheckableItem(altIndicatorItem)),
				noAltIndicatorItem=new CheckableListItem<>(R.string.sk_settings_show_no_alt_indicator, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.showNoAltIndicator, R.drawable.ic_fluent_important_24_regular, i->toggleCheckableItem(noAltIndicatorItem)),
				collapsePostsItem=new CheckableListItem<>(R.string.sk_settings_collapse_long_posts, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.collapseLongPosts, R.drawable.ic_fluent_chevron_down_24_regular, i->toggleCheckableItem(collapsePostsItem)),
				spectatorModeItem=new CheckableListItem<>(R.string.sk_settings_hide_interaction, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.spectatorMode, R.drawable.ic_fluent_star_off_24_regular, i->toggleCheckableItem(spectatorModeItem)),
				hideFabItem=new CheckableListItem<>(R.string.sk_settings_hide_fab, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.autoHideFab, R.drawable.ic_fluent_edit_24_regular, i->toggleCheckableItem(hideFabItem)),
				translateOpenedItem=new CheckableListItem<>(R.string.sk_settings_translate_only_opened, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.translateButtonOpenedOnly, R.drawable.ic_fluent_translate_24_regular, i->toggleCheckableItem(translateOpenedItem)),
				likeIconItem=new CheckableListItem<>(R.string.sk_settings_like_icon, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.likeIcon, R.drawable.ic_fluent_heart_24_regular, i->toggleCheckableItem(likeIconItem)),
				underlinedLinksItem=new CheckableListItem<>(R.string.sk_settings_underlined_links, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.underlinedLinks, R.drawable.ic_fluent_text_underline_24_regular, i->toggleCheckableItem(underlinedLinksItem)),
				showPostDividersItem=new CheckableListItem<>(R.string.mo_enable_dividers, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.showDividers, R.drawable.ic_fluent_timeline_24_regular, i->toggleCheckableItem(showPostDividersItem)),
				disablePillItem=new CheckableListItem<>(R.string.sk_disable_pill_shaped_active_indicator, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.disableM3PillActiveIndicator, R.drawable.ic_fluent_pill_24_regular, i->toggleCheckableItem(disablePillItem)),
				showNavigationLabelsItem=new CheckableListItem<>(R.string.sk_settings_show_labels_in_navigation_bar, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.showNavigationLabels, R.drawable.ic_fluent_tag_24_regular, i->toggleCheckableItem(showNavigationLabelsItem), true),
				pronounsInTimelinesItem=new CheckableListItem<>(R.string.sk_settings_display_pronouns_in_timelines, 0, CheckableListItem.Style.CHECKBOX, GlobalUserPreferences.displayPronounsInTimelines, 0, i->toggleCheckableItem(pronounsInTimelinesItem)),
				pronounsInThreadsItem=new CheckableListItem<>(R.string.sk_settings_display_pronouns_in_threads, 0, CheckableListItem.Style.CHECKBOX, GlobalUserPreferences.displayPronounsInThreads, 0, i->toggleCheckableItem(pronounsInThreadsItem)),
				pronounsInUserListingsItem=new CheckableListItem<>(R.string.sk_settings_display_pronouns_in_user_listings, 0, CheckableListItem.Style.CHECKBOX, GlobalUserPreferences.displayPronounsInUserListings, 0, i->toggleCheckableItem(pronounsInUserListingsItem))
		));
		trueBlackModeItem.checkedChangeListener=checked->onTrueBlackModeClick();
	}

	@Override
	protected void doLoadData(int offset, int count){}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		if(themeTransitionWindowView!=null){
			// Activity has finished recreating. Remove the overlay.
			activity.getSystemService(WindowManager.class).removeView(themeTransitionWindowView);
			themeTransitionWindowView=null;
		}
	}

	@Override
	protected void onHidden(){
		super.onHidden();

		boolean restartPlease=GlobalUserPreferences.disableM3PillActiveIndicator!=disablePillItem.checked
				|| GlobalUserPreferences.showNavigationLabels!=showNavigationLabelsItem.checked
				|| GlobalUserPreferences.showMediaPreview!=showMediaPreviewItem.checked
				|| GlobalUserPreferences.showDividers!=showPostDividersItem.checked
				|| GlobalUserPreferences.likeIcon!=likeIconItem.checked;

		lp.revealCWs=revealCWsItem.checked;
		lp.hideSensitiveMedia=hideSensitiveMediaItem.checked;
		lp.showInteractionCounts=interactionCountsItem.checked;
		lp.customEmojiInNames=emojiInNamesItem.checked;
		lp.save();
		GlobalUserPreferences.toolbarMarquee=marqueeItem.checked;
		GlobalUserPreferences.relocatePublishButton=relocatePublishButtonItem.checked;
		GlobalUserPreferences.reduceMotion=reduceMotionItem.checked;
		GlobalUserPreferences.disableSwipe=disableSwipeItem.checked;
		GlobalUserPreferences.doubleTapToSearch=enableDoubleTapToSearchItem.checked;
		GlobalUserPreferences.doubleTapToSwipe=enableDoubleTapToSwipeItem.checked;
		GlobalUserPreferences.showAltIndicator=altIndicatorItem.checked;
		GlobalUserPreferences.showNoAltIndicator=noAltIndicatorItem.checked;
		GlobalUserPreferences.collapseLongPosts=collapsePostsItem.checked;
		GlobalUserPreferences.spectatorMode=spectatorModeItem.checked;
		GlobalUserPreferences.autoHideFab=hideFabItem.checked;
		GlobalUserPreferences.translateButtonOpenedOnly=translateOpenedItem.checked;
		GlobalUserPreferences.likeIcon=likeIconItem.checked;
		GlobalUserPreferences.underlinedLinks=underlinedLinksItem.checked;
		GlobalUserPreferences.showDividers=showPostDividersItem.checked;
		GlobalUserPreferences.disableM3PillActiveIndicator=disablePillItem.checked;
		GlobalUserPreferences.showNavigationLabels=showNavigationLabelsItem.checked;
		GlobalUserPreferences.displayPronounsInTimelines=pronounsInTimelinesItem.checked;
		GlobalUserPreferences.displayPronounsInThreads=pronounsInThreadsItem.checked;
		GlobalUserPreferences.displayPronounsInUserListings=pronounsInUserListingsItem.checked;
		GlobalUserPreferences.showMediaPreview=showMediaPreviewItem.checked;
		GlobalUserPreferences.enhanceTextSize=enhanceTextSizeItem.checked;
		GlobalUserPreferences.save();
		if(restartPlease) restartActivityToApplyNewTheme();
		else E.post(new StatusDisplaySettingsChangedEvent(accountID));
	}

	private @StringRes int getAppearanceValue(){
		return switch(GlobalUserPreferences.theme){
			case AUTO -> R.string.theme_auto;
			case LIGHT -> R.string.theme_light;
			case DARK -> R.string.theme_dark;
		};
	}

	private String getColorPaletteValue(){
		ColorPreference color=AccountSessionManager.get(accountID).getLocalPreferences().color;
		return color==null
				? getString(R.string.sk_settings_color_palette_default, getString(GlobalUserPreferences.color.getName()))
				: getString(color.getName());
	}

	private String getPublishButtonText() {
		return TextUtils.isEmpty(AccountSessionManager.get(accountID).getLocalPreferences().publishButtonText)
				? getContext().getString(R.string.publish)
				: AccountSessionManager.get(accountID).getLocalPreferences().publishButtonText;
	}

	private @StringRes int getAutoRevealSpoilersText() {
		return switch(GlobalUserPreferences.autoRevealEqualSpoilers){
			case THREADS -> R.string.sk_settings_auto_reveal_author;
			case DISCUSSIONS -> R.string.sk_settings_auto_reveal_anyone;
			default -> R.string.sk_settings_auto_reveal_nobody;
		};
	}

	private void onTrueBlackModeClick(){
		toggleCheckableItem(trueBlackModeItem);
		boolean prev=GlobalUserPreferences.trueBlackTheme;
		GlobalUserPreferences.trueBlackTheme=trueBlackModeItem.checked;
		maybeApplyNewThemeRightNow(null, null, prev);
	}

	private void onEnhanceTextSizeClick(){
		toggleCheckableItem(enhanceTextSizeItem);
		restartActivityToApplyNewTheme();
	}

	private void onAppearanceClick(ListItem<?> item_){
		int selected=switch(GlobalUserPreferences.theme){
			case LIGHT -> 0;
			case DARK -> 1;
			case AUTO -> 2;
		};
		int[] newSelected={selected};
		new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.settings_theme)
				.setSingleChoiceItems((String[])IntStream.of(R.string.theme_light, R.string.theme_dark, R.string.theme_auto).mapToObj(this::getString).toArray(String[]::new),
						selected, (dlg, item)->newSelected[0]=item)
				.setPositiveButton(R.string.ok, (dlg, item)->{
					GlobalUserPreferences.ThemePreference pref=switch(newSelected[0]){
						case 0 -> GlobalUserPreferences.ThemePreference.LIGHT;
						case 1 -> GlobalUserPreferences.ThemePreference.DARK;
						case 2 -> GlobalUserPreferences.ThemePreference.AUTO;
						default -> throw new IllegalStateException("Unexpected value: "+newSelected[0]);
					};
					if(pref!=GlobalUserPreferences.theme){
						GlobalUserPreferences.ThemePreference prev=GlobalUserPreferences.theme;
						GlobalUserPreferences.theme=pref;
						GlobalUserPreferences.save();
						themeItem.subtitleRes=getAppearanceValue();
						rebindItem(themeItem);
						maybeApplyNewThemeRightNow(prev, null, null);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onColorClick(ListItem<?> item_){
		boolean multiple=AccountSessionManager.getInstance().getLoggedInAccounts().size() > 1;
		int indexOffset=multiple ? 1 : 0;
		int selected=lp.color==null ? 0 : lp.color.ordinal() + indexOffset;
		int[] newSelected={selected};
		List<String> items=Arrays.stream(ColorPreference.values()).map(ColorPreference::getName).map(this::getString).collect(Collectors.toList());
		if(multiple)
			items.add(0, getString(R.string.sk_settings_color_palette_default, items.get(GlobalUserPreferences.color.ordinal())));

		Consumer<Boolean> save=(asDefault)->{
			boolean defaultSelected=multiple && newSelected[0]==0;
			ColorPreference pref=defaultSelected ? null : ColorPreference.values()[newSelected[0]-indexOffset];
			if(pref!=lp.color){
				ColorPreference prev=lp.color;
				lp.color=asDefault ? null : pref;
				lp.save();
				if((asDefault || !multiple) && pref!=null){
					GlobalUserPreferences.color=pref;
					GlobalUserPreferences.save();
				}
				colorItem.subtitle=getColorPaletteValue();
				rebindItem(colorItem);
				if(prev==null && pref!=null) restartActivityToApplyNewTheme();
				else maybeApplyNewThemeRightNow(null, prev, null);
			}
		};

		AlertDialog.Builder alert=new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.sk_settings_color_palette)
				.setSingleChoiceItems(items.stream().toArray(String[]::new),
						selected, (dlg, item)->newSelected[0]=item)
				.setPositiveButton(R.string.ok, (dlg, item)->save.accept(false))
				.setNegativeButton(R.string.cancel, null);
		if(multiple) alert.setNeutralButton(R.string.sk_set_as_default, (dlg, item)->save.accept(true));
		alert.show();
	}

	private void onPublishTextClick(ListItem<?> item_){
		TextInputFrameLayout input = new TextInputFrameLayout(
				getContext(),
				getString(R.string.publish),
				TextUtils.isEmpty(lp.publishButtonText) ? "" : lp.publishButtonText.trim()
		);
		new M3AlertDialogBuilder(getContext()).setTitle(R.string.sk_settings_publish_button_text_title).setView(input)
				.setPositiveButton(R.string.save, (d, which) -> {
					lp.publishButtonText=input.getEditText().getText().toString().trim();
					lp.save();
					publishTextItem.subtitle=getPublishButtonText();
					rebindItem(publishTextItem);
				})
				.setNeutralButton(R.string.clear, (d, which) -> {
					lp.publishButtonText=null;
					lp.save();
					publishTextItem.subtitle=getPublishButtonText();
					rebindItem(publishTextItem);
				})
				.setNegativeButton(R.string.cancel, (d, which) -> {})
				.show();
	}

	private void onAutoRevealSpoilersClick(ListItem<?> item_){
		int selected=GlobalUserPreferences.autoRevealEqualSpoilers.ordinal();
		int[] newSelected={selected};
		new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.sk_settings_auto_reveal_equal_spoilers)
				.setSingleChoiceItems((String[])IntStream.of(R.string.sk_settings_auto_reveal_nobody, R.string.sk_settings_auto_reveal_author, R.string.sk_settings_auto_reveal_anyone).mapToObj(this::getString).toArray(String[]::new),
						selected, (dlg, item)->newSelected[0]=item)
				.setPositiveButton(R.string.ok, (dlg, item)->{
					GlobalUserPreferences.autoRevealEqualSpoilers=GlobalUserPreferences.AutoRevealMode.values()[newSelected[0]];
					autoRevealCWsItem.subtitleRes=getAutoRevealSpoilersText();
					rebindItem(autoRevealCWsItem);
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void maybeApplyNewThemeRightNow(GlobalUserPreferences.ThemePreference prevTheme, ColorPreference prevColor, Boolean prevTrueBlack){
		if(prevTheme==null) prevTheme=GlobalUserPreferences.theme;
		if(prevTrueBlack==null) prevTrueBlack=GlobalUserPreferences.trueBlackTheme;
		if(prevColor==null) prevColor=lp.getCurrentColor();

		boolean isCurrentDark=prevTheme==GlobalUserPreferences.ThemePreference.DARK ||
				(prevTheme==GlobalUserPreferences.ThemePreference.AUTO && Build.VERSION.SDK_INT>=30 && getResources().getConfiguration().isNightModeActive());
		boolean isNewDark=GlobalUserPreferences.theme==GlobalUserPreferences.ThemePreference.DARK ||
				(GlobalUserPreferences.theme==GlobalUserPreferences.ThemePreference.AUTO && Build.VERSION.SDK_INT>=30 && getResources().getConfiguration().isNightModeActive());
		boolean isNewBlack=GlobalUserPreferences.trueBlackTheme;
		if(isCurrentDark!=isNewDark || prevColor!=lp.getCurrentColor() || (isNewDark && prevTrueBlack!=isNewBlack)){
			restartActivityToApplyNewTheme();
		}
	}

	private void restartActivityToApplyNewTheme(){
		// Calling activity.recreate() causes a black screen for like half a second.
		// So, let's take a screenshot and overlay it on top to create the illusion of a smoother transition.
		// As a bonus, we can fade it out to make it even smoother.
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N && Build.VERSION.SDK_INT<Build.VERSION_CODES.S){
			View activityDecorView=getActivity().getWindow().getDecorView();
			Bitmap bitmap=Bitmap.createBitmap(activityDecorView.getWidth(), activityDecorView.getHeight(), Bitmap.Config.ARGB_8888);
			activityDecorView.draw(new Canvas(bitmap));
			themeTransitionWindowView=new ImageView(MastodonApp.context);
			themeTransitionWindowView.setImageBitmap(bitmap);
			WindowManager.LayoutParams lp=new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION);
			lp.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
					WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
			lp.systemUiVisibility=View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			lp.systemUiVisibility|=(activityDecorView.getWindowSystemUiVisibility() & (View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
			lp.width=lp.height=WindowManager.LayoutParams.MATCH_PARENT;
			lp.token=getActivity().getWindow().getAttributes().token;
			lp.windowAnimations=R.style.window_fade_out;
			MastodonApp.context.getSystemService(WindowManager.class).addView(themeTransitionWindowView, lp);
		}
		getActivity().recreate();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		((FragmentStackActivity)getActivity()).invalidateSystemBarColors(this);
	}
}
