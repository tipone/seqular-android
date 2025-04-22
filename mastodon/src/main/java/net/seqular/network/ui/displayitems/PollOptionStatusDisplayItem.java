package net.seqular.network.ui.displayitems;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import net.seqular.network.R;
import net.seqular.network.fragments.BaseStatusListFragment;
import net.seqular.network.model.Poll;
import net.seqular.network.model.Status;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.text.HtmlParser;
import net.seqular.network.ui.utils.CustomEmojiHelper;
import net.seqular.network.ui.utils.UiUtils;

import java.util.Locale;

import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;

public class PollOptionStatusDisplayItem extends StatusDisplayItem{
	private CharSequence text;
	private CharSequence translatedText;
	public final Poll.Option option;
	private CustomEmojiHelper emojiHelper=new CustomEmojiHelper();
	public boolean showResults;
	public boolean isAnimating;
	private float votesFraction; // 0..1
	private boolean isMostVoted;
	private final int optionIndex;
	public final Poll poll;
	public final Status status;


	public PollOptionStatusDisplayItem(String parentID, Poll poll, int optionIndex, BaseStatusListFragment parentFragment, Status status){
		super(parentID, parentFragment);
		this.optionIndex=optionIndex;
		option=poll.options.get(optionIndex);
		this.poll=poll;
		this.status=status;
		text=HtmlParser.parseCustomEmoji(option.title, poll.emojis);
		emojiHelper.setText(text);
		showResults=poll.isExpired() || poll.voted;
		calculateResults();
	}

	private void calculateResults() {
		int total=poll.votersCount>0 ? poll.votersCount : poll.votesCount;
		if(showResults && option.votesCount!=null && total>0){
			votesFraction=(float)option.votesCount/(float)total;
			int mostVotedCount=0;
			for(Poll.Option opt:poll.options)
				mostVotedCount=Math.max(mostVotedCount, opt.votesCount);
			isMostVoted=option.votesCount==mostVotedCount;
		}
	}

	@Override
	public Type getType(){
		return Type.POLL_OPTION;
	}

	@Override
	public int getImageCount(){
		return emojiHelper.getImageCount();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return emojiHelper.getImageRequest(index);
	}

	public static class Holder extends StatusDisplayItem.Holder<PollOptionStatusDisplayItem> implements ImageLoaderViewHolder{
		private final TextView text, percent;
		private final View button;
		private final ImageView icon;
		private final Drawable progressBg;
		private static final int ANIMATION_DURATION=500;

		public Holder(Activity activity, ViewGroup parent){
			super(activity, R.layout.display_item_poll_option, parent);
			text=findViewById(R.id.text);
			percent=findViewById(R.id.percent);
			icon=findViewById(R.id.icon);
			button=findViewById(R.id.button);
			progressBg=activity.getResources().getDrawable(R.drawable.bg_poll_option_voted, activity.getTheme()).mutate();
			itemView.setOnClickListener(this::onButtonClick);
			button.setOutlineProvider(OutlineProviders.roundedRect(20));
			button.setClipToOutline(true);
		}

		@Override
		public void onBind(PollOptionStatusDisplayItem item){
			if (item.status.translation != null && item.status.translationState == Status.TranslationState.SHOWN) {
				if(item.translatedText==null){
					item.translatedText=item.status.translation.poll.options[item.optionIndex].title;
				}
				text.setText(item.translatedText);
			} else {
				text.setText(item.text);
			}
			percent.setVisibility(item.showResults ? View.VISIBLE : View.GONE);
			itemView.setClickable(!item.showResults);
			icon.setImageDrawable(itemView.getContext().getDrawable(item.poll.multiple ?
					item.showResults ? R.drawable.ic_poll_checkbox_regular_selector : R.drawable.ic_poll_checkbox_filled_selector :
					item.showResults ? R.drawable.ic_poll_option_button : R.drawable.ic_fluent_radio_button_24_selector
			));
			if(item.showResults){
				Drawable bg=progressBg;
				bg.setLevel(Math.round(10000f*item.votesFraction));
				button.setBackground(bg);
				itemView.setSelected(item.poll.ownVotes!=null && item.poll.ownVotes.contains(item.optionIndex));
				percent.setText(String.format(Locale.getDefault(), "%d%%", Math.round(item.votesFraction*100f)));
			}else{
				itemView.setSelected(item.poll.selectedOptions!=null && item.poll.selectedOptions.contains(item.option));
				button.setBackgroundResource(R.drawable.bg_poll_option_clickable);
			}
			text.setTextColor(UiUtils.getThemeColor(itemView.getContext(), android.R.attr.textColorPrimary));
			percent.setTextColor(UiUtils.getThemeColor(itemView.getContext(), R.attr.colorM3OnSecondaryContainer));

			if (item.isAnimating) {
				showResults(item.showResults);
				item.isAnimating= false;
			}
		}

		@Override
		public void setImage(int index, Drawable image){
			item.emojiHelper.setImageDrawable(index, image);
			text.setText(text.getText());
			if(image instanceof Animatable){
				((Animatable) image).start();
			}
		}

		@Override
		public void clearImage(int index){
			item.emojiHelper.setImageDrawable(index, null);
			text.setText(text.getText());
		}

		private void onButtonClick(View v){
			item.parentFragment.onPollOptionClick(this);
		}

		public void showResults(boolean shown) {
			item.showResults = shown;
			item.calculateResults();
			Drawable bg=progressBg;
			long animationDuration = (long) (ANIMATION_DURATION*item.votesFraction);
			int startLevel=shown ? 0 : progressBg.getLevel();
			int targetLevel=shown ? Math.round(10000f*item.votesFraction) : 0;
			ObjectAnimator animator=ObjectAnimator.ofInt(bg, "level", startLevel, targetLevel);
			animator.setDuration(animationDuration);
			animator.setInterpolator(new DecelerateInterpolator());
			button.setBackground(bg);
			if(shown){
				itemView.setSelected(item.poll.ownVotes!=null && item.poll.ownVotes.contains(item.optionIndex));
				// animate percent
				percent.setVisibility(View.VISIBLE);
				ValueAnimator percentAnimation=ValueAnimator.ofInt(0, Math.round(100f*item.votesFraction));
				percentAnimation.setDuration(animationDuration);
				percentAnimation.setInterpolator(new DecelerateInterpolator());
				percentAnimation.addUpdateListener(animation -> percent.setText(String.format(Locale.getDefault(), "%d%%", (int) animation.getAnimatedValue())));
				percentAnimation.start();
			}else{
				animator.addListener(new AnimatorListenerAdapter(){
					@Override
					public void onAnimationEnd(Animator animation){
						button.setBackgroundResource(R.drawable.bg_poll_option_clickable);
					}
				});
				itemView.setSelected(item.poll.selectedOptions!=null && item.poll.selectedOptions.contains(item.option));
				percent.setVisibility(View.GONE);
			}
			animator.start();
		}
	}
}
