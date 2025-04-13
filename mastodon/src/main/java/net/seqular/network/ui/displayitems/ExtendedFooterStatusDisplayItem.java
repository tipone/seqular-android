package net.seqular.network.ui.displayitems;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.R;
import net.seqular.network.fragments.BaseStatusListFragment;
import net.seqular.network.fragments.StatusEditHistoryFragment;
import net.seqular.network.fragments.ThreadFragment;
import net.seqular.network.fragments.account_list.StatusFavoritesListFragment;
import net.seqular.network.fragments.account_list.StatusReblogsListFragment;
import net.seqular.network.fragments.account_list.StatusRelatedAccountListFragment;
import net.seqular.network.model.Status;
import net.seqular.network.ui.Snackbar;
import net.seqular.network.ui.utils.UiUtils;
import org.parceler.Parcels;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import me.grishka.appkit.Nav;

public class ExtendedFooterStatusDisplayItem extends StatusDisplayItem{
	public final String accountID;

	private static final DateTimeFormatter TIME_FORMATTER=DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
	private static final DateTimeFormatter TIME_FORMATTER_LONG=DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
	private static final DateTimeFormatter DATE_FORMATTER=DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

	public ExtendedFooterStatusDisplayItem(String parentID, BaseStatusListFragment parentFragment, String accountID, Status status){
		super(parentID, parentFragment);
		this.status=status;
		this.accountID=accountID;
	}

	@Override
	public Type getType(){
		return Type.EXTENDED_FOOTER;
	}

	public static class Holder extends StatusDisplayItem.Holder<ExtendedFooterStatusDisplayItem>{
		private final TextView time, date, app, dateAppSeparator;
		private final TextView favorites, reblogs, editHistory;
		private final ImageView visibility;
		private final Context context;

		public Holder(Context context, ViewGroup parent){
			super(context, R.layout.display_item_extended_footer, parent);
			this.context = context;
			reblogs=findViewById(R.id.reblogs);
			favorites=findViewById(R.id.favorites);
			editHistory=findViewById(R.id.edit_history);
			time=findViewById(R.id.time);
			date=findViewById(R.id.date);
			app=findViewById(R.id.app_name);
			visibility=findViewById(R.id.visibility);
			dateAppSeparator=findViewById(R.id.date_app_separator);

			reblogs.setOnClickListener(v->startAccountListFragment(StatusReblogsListFragment.class));
			favorites.setOnClickListener(v->startAccountListFragment(StatusFavoritesListFragment.class));
			editHistory.setOnClickListener(v->startEditHistoryFragment());
			time.setOnClickListener(v->showTimeSnackbar());
			app.setOnClickListener(v->UiUtils.launchWebBrowser(context, item.status.application.website));
		}

		@SuppressLint("DefaultLocale")
		@Override
		public void onBind(ExtendedFooterStatusDisplayItem item){
			Status s=item.status;
			favorites.setText(getFormattedPlural(R.plurals.x_favorites, item.status.favouritesCount));
			favorites.setCompoundDrawablesRelativeWithIntrinsicBounds(GlobalUserPreferences.likeIcon ? R.drawable.ic_fluent_heart_20_regular : R.drawable.ic_fluent_star_20_regular, 0, 0, 0);
			reblogs.setText(getFormattedPlural(R.plurals.x_reblogs, item.status.reblogsCount));
			if(s.editedAt!=null){
				editHistory.setVisibility(View.VISIBLE);
				ZonedDateTime dt=s.editedAt.atZone(ZoneId.systemDefault());
				String time=TIME_FORMATTER.format(dt);
				if(!dt.toLocalDate().equals(LocalDate.now())){
					time+=" · "+DATE_FORMATTER.format(dt);
				}
				editHistory.setText(getFormattedSubstitutedString(R.string.last_edit_at_x, time));
			}else{
				editHistory.setVisibility(View.GONE);
			}
			ZonedDateTime dt=item.status.createdAt.atZone(ZoneId.systemDefault());
			time.setText(TIME_FORMATTER.format(dt));
			date.setText(DATE_FORMATTER.format(dt));
			if(item.status.application!=null && !TextUtils.isEmpty(item.status.application.name)){
				app.setVisibility(View.VISIBLE);
				dateAppSeparator.setVisibility(View.VISIBLE);
				app.setText(item.status.application.name);
				app.setEnabled(!TextUtils.isEmpty(item.status.application.website));
			}else{
				app.setVisibility(View.GONE);
				dateAppSeparator.setVisibility(View.GONE);
			}

			//TODO: make a snackbar pop up on hold of this
			visibility.setImageResource(switch (s.visibility) {
				case PUBLIC -> R.drawable.ic_fluent_earth_20_regular;
				case UNLISTED -> R.drawable.ic_fluent_lock_open_20_regular;
				case PRIVATE -> R.drawable.ic_fluent_lock_closed_20_filled;
				case DIRECT -> R.drawable.ic_fluent_mention_20_regular;
				case LOCAL -> R.drawable.ic_fluent_eye_20_regular;
			});
		}

		@Override
		public boolean isEnabled(){
			return false;
		}

		private SpannableStringBuilder getFormattedPlural(@PluralsRes int res, long quantity){
			String str=item.parentFragment.getResources().getQuantityString(res, (int)quantity, quantity);
			String formattedNumber=String.format(Locale.getDefault(), "%,d", quantity);
			int index=str.indexOf(formattedNumber);
			SpannableStringBuilder ssb=new SpannableStringBuilder(str);
			if(index>=0){
				ForegroundColorSpan colorSpan=new ForegroundColorSpan(UiUtils.getThemeColor(itemView.getContext(), R.attr.colorM3OnSurfaceVariant));
				ssb.setSpan(colorSpan, index, index+formattedNumber.length(), 0);
				Object typefaceSpan;
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
					typefaceSpan=new TypefaceSpan(Typeface.create(Typeface.DEFAULT, 600, false));
				}else{
					typefaceSpan=new StyleSpan(Typeface.BOLD);
				}
				ssb.setSpan(typefaceSpan, index, index+formattedNumber.length(), 0);
			}
			return ssb;
		}

		private SpannableStringBuilder getFormattedSubstitutedString(@StringRes int res, String substitution){
			String str=item.parentFragment.getString(res, substitution);
			int index=item.parentFragment.getString(res).indexOf("%s");
			SpannableStringBuilder ssb=new SpannableStringBuilder(str);
			if(index>=0){
				ForegroundColorSpan colorSpan=new ForegroundColorSpan(UiUtils.getThemeColor(itemView.getContext(), R.attr.colorM3OnSurfaceVariant));
				ssb.setSpan(colorSpan, index, index+substitution.length(), 0);
				Object typefaceSpan;
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
					typefaceSpan=new TypefaceSpan(Typeface.create(Typeface.DEFAULT, 600, false));
				}else{
					typefaceSpan=new StyleSpan(Typeface.BOLD);
				}
				ssb.setSpan(typefaceSpan, index, index+substitution.length(), 0);
			}
			return ssb;
		}

		private void startAccountListFragment(Class<? extends StatusRelatedAccountListFragment> cls){
			if(item.status.preview) return;
			Bundle args=new Bundle();
			args.putString("account", item.parentFragment.getAccountID());
			args.putParcelable("status", Parcels.wrap(item.status));
			Nav.go(item.parentFragment.getActivity(), cls, args);
		}

		private void startEditHistoryFragment(){
			if(item.status.preview) return;
			Bundle args=new Bundle();
			args.putString("account", item.parentFragment.getAccountID());
			args.putString("id", item.status.id);
			args.putString("url", item.status.url);
			Nav.go(item.parentFragment.getActivity(), StatusEditHistoryFragment.class, args);
		}

		private void showTimeSnackbar(){
			int bottomOffset=0;
			if(item.parentFragment instanceof ThreadFragment tf){
				bottomOffset=tf.getSnackbarOffset();
			}
			new Snackbar.Builder(itemView.getContext())
					.setText(itemView.getContext().getString(R.string.posted_at, TIME_FORMATTER_LONG.format(item.status.createdAt.atZone(ZoneId.systemDefault()))))
					.setBottomOffset(bottomOffset)
					.show();
		}
	}
}
