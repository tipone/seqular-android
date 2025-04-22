package net.seqular.network.fragments.discover;

import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.seqular.network.R;
import net.seqular.network.api.requests.accounts.GetAccountRelationships;
import net.seqular.network.api.requests.accounts.GetFollowSuggestions;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.fragments.IsOnTop;
import net.seqular.network.fragments.MastodonRecyclerFragment;
import net.seqular.network.fragments.ProfileFragment;
import net.seqular.network.fragments.ScrollableToTop;
import net.seqular.network.model.Account;
import net.seqular.network.model.FollowSuggestion;
import net.seqular.network.model.Relationship;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.text.HtmlParser;
import net.seqular.network.ui.utils.CustomEmojiHelper;
import net.seqular.network.ui.utils.UiUtils;
import net.seqular.network.ui.views.ProgressBarButton;
import net.seqular.network.utils.ProvidesAssistContent;
import org.parceler.Parcels;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.UsableRecyclerView;

public class DiscoverAccountsFragment extends MastodonRecyclerFragment<DiscoverAccountsFragment.AccountWrapper> implements ScrollableToTop, IsOnTop, ProvidesAssistContent.ProvidesWebUri {
	private String accountID;
	private Map<String, Relationship> relationships=Collections.emptyMap();
	private GetAccountRelationships relationshipsRequest;

	public DiscoverAccountsFragment(){
		super(20);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		accountID=getArguments().getString("account");
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
			setRetainInstance(true);
	}

	@Override
	protected void doLoadData(int offset, int count){
		if(relationshipsRequest!=null){
			relationshipsRequest.cancel();
			relationshipsRequest=null;
		}
		currentRequest=new GetFollowSuggestions(count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<FollowSuggestion> result){
						if(getActivity()==null) return;
						onDataLoaded(result.stream().map(fs->new AccountWrapper(fs.account)).collect(Collectors.toList()), false);
						loadRelationships();
					}
				})
				.exec(accountID);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		return new AccountsAdapter();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		list.addItemDecoration(new RecyclerView.ItemDecoration(){
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
				outRect.bottom=outRect.left=outRect.right=V.dp(16);
				if(parent.getChildAdapterPosition(view)==0)
					outRect.top=V.dp(16);
			}
		});
		((UsableRecyclerView)list).setDrawSelectorOnTop(true);
	}

	private void loadRelationships(){
		relationships=Collections.emptyMap();
		relationshipsRequest=new GetAccountRelationships(data.stream().map(fs->fs.account.id).collect(Collectors.toList()));
		relationshipsRequest.setCallback(new Callback<>(){
			@Override
			public void onSuccess(List<Relationship> result){
				relationshipsRequest=null;
				relationships=result.stream().collect(Collectors.toMap(rel->rel.id, Function.identity()));
				if(getActivity()==null) return;
				if(list==null)
					return;
				for(int i=0;i<list.getChildCount();i++){
					RecyclerView.ViewHolder holder=list.getChildViewHolder(list.getChildAt(i));
					if(holder instanceof AccountViewHolder avh)
						avh.rebind();
				}
			}

			@Override
			public void onError(ErrorResponse error){
				relationshipsRequest=null;
			}
		}).exec(accountID);
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		if(relationshipsRequest!=null){
			relationshipsRequest.cancel();
			relationshipsRequest=null;
		}
	}

	@Override
	public void scrollToTop(){
		smoothScrollRecyclerViewToTop(list);
	}

	@Override
	public boolean isOnTop() {
		return isRecyclerViewOnTop(list);
	}

	@Override
	public String getAccountID() {
		return accountID;
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return isInstanceAkkoma() ? null : base.path("/explore/suggestions").build();
	}

	private class AccountsAdapter extends UsableRecyclerView.Adapter<AccountViewHolder> implements ImageLoaderRecyclerAdapter{

		public AccountsAdapter(){
			super(imgLoader);
		}

		@Override
		public void onBindViewHolder(AccountViewHolder holder, int position){
			holder.bind(data.get(position));
			super.onBindViewHolder(holder, position);
		}

		@NonNull
		@Override
		public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new AccountViewHolder();
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public int getImageCountForItem(int position){
			return 2+data.get(position).emojiHelper.getImageCount();
		}

		@Override
		public ImageLoaderRequest getImageRequest(int position, int image){
			AccountWrapper item=data.get(position);
			if(image==0)
				return item.avaRequest;
			else if(image==1)
				return item.coverRequest;
			else
				return item.emojiHelper.getImageRequest(image-2);
		}
	}

	private class AccountViewHolder extends BindableViewHolder<AccountWrapper> implements ImageLoaderViewHolder, UsableRecyclerView.DisableableClickable{
		private final ImageView cover, avatar;
		private final TextView name, username, bio, followersCount, followingCount, postsCount, followersLabel, followingLabel, postsLabel;
		private final ProgressBarButton actionButton;
		private final ProgressBar actionProgress;
		private final View actionWrap;

		private Relationship relationship;

		public AccountViewHolder(){
			super(getActivity(), R.layout.item_discover_account, list);
			cover=findViewById(R.id.cover);
			avatar=findViewById(R.id.avatar);
			name=findViewById(R.id.name);
			username=findViewById(R.id.username);
			bio=findViewById(R.id.bio);
			followersCount=findViewById(R.id.followers_count);
			followersLabel=findViewById(R.id.followers_label);
			followingCount=findViewById(R.id.following_count);
			followingLabel=findViewById(R.id.following_label);
			postsCount=findViewById(R.id.posts_count);
			postsLabel=findViewById(R.id.posts_label);
			actionButton=findViewById(R.id.action_btn);
			actionProgress=findViewById(R.id.action_progress);
			actionWrap=findViewById(R.id.action_btn_wrap);

			avatar.setOutlineProvider(OutlineProviders.roundedRect(15));
			avatar.setClipToOutline(true);
			View border=findViewById(R.id.avatar_border);
			border.setOutlineProvider(OutlineProviders.roundedRect(17));
			border.setClipToOutline(true);
			cover.setOutlineProvider(OutlineProviders.roundedRect(9));
			cover.setClipToOutline(true);
			itemView.setOutlineProvider(OutlineProviders.roundedRect(12));
			itemView.setClipToOutline(true);
			actionButton.setOnClickListener(this::onActionButtonClick);
			itemView.setOnClickListener(v->this.onClick());
		}

		@Override
		public boolean isEnabled(){
			return false;
		}

		@Override
		public void onBind(AccountWrapper item){
			name.setText(item.parsedName);
			username.setText('@'+item.account.acct);
			bio.setText(item.parsedBio);
			followersCount.setText(UiUtils.abbreviateNumber(item.account.followersCount));
			followingCount.setText(UiUtils.abbreviateNumber(item.account.followingCount));
			postsCount.setText(UiUtils.abbreviateNumber(item.account.statusesCount));
			followersLabel.setText(getResources().getQuantityString(R.plurals.followers, (int)Math.min(999, item.account.followersCount)));
			followingLabel.setText(getResources().getQuantityString(R.plurals.following, (int)Math.min(999, item.account.followingCount)));
			postsLabel.setText(getResources().getQuantityString(R.plurals.sk_posts_count_label, (int)(item.account.statusesCount%1000), item.account.statusesCount));
			followersCount.setVisibility(item.account.followersCount < 0 ? View.GONE : View.VISIBLE);
			followersLabel.setVisibility(item.account.followersCount < 0 ? View.GONE : View.VISIBLE);
			followingCount.setVisibility(item.account.followingCount < 0 ? View.GONE : View.VISIBLE);
			followingLabel.setVisibility(item.account.followingCount < 0 ? View.GONE : View.VISIBLE);
			relationship=relationships.get(item.account.id);
			UiUtils.setExtraTextInfo(getContext(), null, true, false, false, item.account);

			if(relationship==null){
				actionWrap.setVisibility(View.GONE);
			}else{
				actionWrap.setVisibility(View.VISIBLE);
				UiUtils.setRelationshipToActionButtonM3(relationship, actionButton);
			}
		}

		@Override
		public void setImage(int index, Drawable image){
			if(index==0){
				avatar.setImageDrawable(image);
			}else if(index==1){
				cover.setImageDrawable(image);
			}else{
				item.emojiHelper.setImageDrawable(index-2, image);
				name.setText(name.getText());
				bio.setText(bio.getText());
			}
			if(image instanceof Animatable a && !a.isRunning())
				a.start();
		}

		@Override
		public void clearImage(int index){
			setImage(index, null);
		}

		@Override
		public void onClick(){
			Bundle args=new Bundle();
			args.putString("account", accountID);
			args.putParcelable("profileAccount", Parcels.wrap(item.account));
			Nav.go(getActivity(), ProfileFragment.class, args);
		}

		private void onActionButtonClick(View v){
			itemView.setHasTransientState(true);
			UiUtils.performAccountAction(getActivity(), item.account, accountID, relationship, actionButton, this::setActionProgressVisible, rel->{
				itemView.setHasTransientState(false);
				relationships.put(item.account.id, rel);
				rebind();
			});
		}

		private void setActionProgressVisible(boolean visible){
			actionButton.setTextVisible(!visible);
			actionProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
			if(visible)
				actionProgress.setIndeterminateTintList(actionButton.getTextColors());
			actionButton.setClickable(!visible);
		}
	}

	protected class AccountWrapper{
		public Account account;
		public ImageLoaderRequest avaRequest, coverRequest;
		public CustomEmojiHelper emojiHelper=new CustomEmojiHelper();
		public CharSequence parsedName, parsedBio;

		public AccountWrapper(Account account){
			this.account=account;
			avaRequest=new UrlImageLoaderRequest(
					TextUtils.isEmpty(account.avatar) ? AccountSessionManager.getInstance().getAccount(accountID).getDefaultAvatarUrl() : account.avatar,
					V.dp(50), V.dp(50));
			if(!TextUtils.isEmpty(account.header))
				coverRequest=new UrlImageLoaderRequest(account.header, 1000, 1000);
			parsedBio=HtmlParser.parse(account.note, account.emojis, Collections.emptyList(), Collections.emptyList(), accountID);
			if(account.emojis.isEmpty()){
				parsedName= account.getDisplayName();
			}else{
				parsedName=HtmlParser.parseCustomEmoji(account.getDisplayName(), account.emojis);
				emojiHelper.setText(new SpannableStringBuilder(parsedName).append(parsedBio));
			}
		}
	}
}
