package net.seqular.network.fragments.onboarding;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import net.seqular.network.R;
import net.seqular.network.api.requests.accounts.UpdateAccountCredentials;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.fragments.HomeFragment;
import net.seqular.network.model.Account;
import net.seqular.network.model.viewmodel.CheckableListItem;
import net.seqular.network.ui.M3AlertDialogBuilder;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.adapters.GenericListItemsAdapter;
import net.seqular.network.ui.utils.UiUtils;
import net.seqular.network.ui.viewholders.ListItemViewHolder;
import net.seqular.network.utils.ElevationOnScrollListener;

import java.util.List;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.fragments.ToolbarFragment;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.FragmentRootLinearLayout;

public class OnboardingProfileSetupFragment extends ToolbarFragment{
	private Button btn;
	private View buttonBar;
	private String accountID;
	private ElevationOnScrollListener onScrollListener;
	private ScrollView scroller;
	private EditText nameEdit, bioEdit;
	private ImageView avaImage, coverImage;
	private Uri avatarUri, coverUri;
	private LinearLayout scrollContent;
	private CheckableListItem<Void> discoverableItem;

	private static final int AVATAR_RESULT=348;
	private static final int COVER_RESULT=183;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setNavigationBarColor(UiUtils.getThemeColor(activity, R.attr.colorM3Surface));
		accountID=getArguments().getString("account");
		setTitle(R.string.profile_setup);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view=inflater.inflate(R.layout.fragment_onboarding_profile_setup, container, false);

		scroller=view.findViewById(R.id.scroller);
		nameEdit=view.findViewById(R.id.display_name);
		bioEdit=view.findViewById(R.id.bio);
		avaImage=view.findViewById(R.id.avatar);
		coverImage=view.findViewById(R.id.header);

		btn=view.findViewById(R.id.btn_next);
		btn.setOnClickListener(v->onButtonClick());
		buttonBar=view.findViewById(R.id.button_bar);

		avaImage.setOutlineProvider(OutlineProviders.roundedRect(24));
		avaImage.setClipToOutline(true);

		Account account=AccountSessionManager.getInstance().getAccount(accountID).self;
		if(savedInstanceState==null){
			nameEdit.setText(account.displayName);
		}

		avaImage.setOnClickListener(v->startActivityForResult(UiUtils.getMediaPickerIntent(new String[]{"image/*"}, 1), AVATAR_RESULT));
		coverImage.setOnClickListener(v->startActivityForResult(UiUtils.getMediaPickerIntent(new String[]{"image/*"}, 1), COVER_RESULT));

		scrollContent=view.findViewById(R.id.scrollable_content);
		discoverableItem=new CheckableListItem<>(R.string.make_profile_discoverable, 0, CheckableListItem.Style.SWITCH_SEPARATED, true, R.drawable.ic_campaign_24px, item->showDiscoverabilityAlert());
		GenericListItemsAdapter<Void> fakeAdapter=new GenericListItemsAdapter<>(List.of(discoverableItem));
		ListItemViewHolder<?> holder=fakeAdapter.onCreateViewHolder(scrollContent, fakeAdapter.getItemViewType(0));
		fakeAdapter.bindViewHolder(holder, 0);
		holder.itemView.setBackground(UiUtils.getThemeDrawable(getActivity(), android.R.attr.selectableItemBackground));
		holder.itemView.setOnClickListener(v->holder.onClick());
		scrollContent.addView(holder.itemView);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		scroller.setOnScrollChangeListener(onScrollListener=new ElevationOnScrollListener((FragmentRootLinearLayout) view, buttonBar, getToolbar()));
	}

	@Override
	protected void onUpdateToolbar(){
		super.onUpdateToolbar();
		if(onScrollListener!=null){
			onScrollListener.setViews(buttonBar, getToolbar());
		}
	}

	protected void onButtonClick(){
		new UpdateAccountCredentials(nameEdit.getText().toString(), bioEdit.getText().toString(), avatarUri, coverUri, null)
				.setDiscoverableIndexable(discoverableItem.checked, discoverableItem.checked)
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(Account result){
						AccountSessionManager.getInstance().updateAccountInfo(accountID, result);
						Bundle args=new Bundle();
						args.putString("account", accountID);
						Nav.goClearingStack(getActivity(), HomeFragment.class, args);
					}

					@Override
					public void onError(ErrorResponse error){
						error.showToast(getActivity());
					}
				})
				.wrapProgress(getActivity(), R.string.saving, true)
				.exec(accountID);
	}

	@Override
	public void onApplyWindowInsets(WindowInsets insets){
		super.onApplyWindowInsets(UiUtils.applyBottomInsetToFixedView(buttonBar, insets));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode!=Activity.RESULT_OK)
			return;
		ImageView img;
		Uri uri=data.getData();
		int size;
		if(requestCode==AVATAR_RESULT){
			img=avaImage;
			avatarUri=uri;
			size=V.dp(100);
		}else{
			img=coverImage;
			coverUri=uri;
			size=V.dp(1000);
		}
		img.setForeground(null);
		ViewImageLoader.load(img, null, new UrlImageLoaderRequest(uri, size, size));
	}

	private void showDiscoverabilityAlert(){
		new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.discoverability)
				.setMessage(R.string.discoverability_help)
				.setPositiveButton(R.string.ok, null)
				.show();
	}
}
