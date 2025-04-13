package net.seqular.network.fragments.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import net.seqular.network.BuildConfig;
import net.seqular.network.E;
import net.seqular.network.MainActivity;
import net.seqular.network.R;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.events.SelfUpdateStateChangedEvent;
import net.seqular.network.model.Instance;
import net.seqular.network.model.viewmodel.ListItem;
import net.seqular.network.ui.sheets.AccountSwitcherSheet;
import net.seqular.network.ui.M3AlertDialogBuilder;
import net.seqular.network.ui.utils.HideableSingleViewRecyclerAdapter;
import net.seqular.network.ui.utils.UiUtils;
import net.seqular.network.updater.GithubSelfUpdater;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.utils.MergeRecyclerAdapter;

public class SettingsMainFragment extends BaseSettingsFragment<Void>{
	private AccountSession account;
	private boolean loggedOut;
	private HideableSingleViewRecyclerAdapter bannerAdapter;
	private Button updateButton1, updateButton2;
	private TextView updateText;
	private Runnable updateDownloadProgressUpdater=new Runnable(){
		@Override
		public void run(){
			GithubSelfUpdater.UpdateState state=GithubSelfUpdater.getInstance().getState();
			if(state==GithubSelfUpdater.UpdateState.DOWNLOADING){
				updateButton1.setText(getString(R.string.downloading_update, Math.round(GithubSelfUpdater.getInstance().getDownloadProgress()*100f)));
				list.postDelayed(this, 250);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		account=AccountSessionManager.get(accountID);
		setTitle(R.string.settings);
		setSubtitle(account.getFullUsername());
		onDataLoaded(List.of(
				new ListItem<>(R.string.settings_behavior, 0, R.drawable.ic_fluent_settings_24_regular, this::onBehaviorClick),
				new ListItem<>(R.string.settings_display, 0, R.drawable.ic_fluent_color_24_regular, this::onDisplayClick),
				new ListItem<>(R.string.settings_privacy, 0, R.drawable.ic_fluent_shield_24_regular, this::onPrivacyClick),
				new ListItem<>(R.string.settings_notifications, 0, R.drawable.ic_fluent_alert_24_regular, this::onNotificationsClick),
				new ListItem<>(R.string.sk_settings_instance, 0, R.drawable.ic_fluent_server_24_regular, this::onInstanceClick),
				new ListItem<>(getString(R.string.about_app, getString(R.string.mo_app_name)), null, R.drawable.ic_fluent_info_24_regular, this::onAboutClick, null, 0, true),
				new ListItem<>(R.string.manage_accounts, 0, R.drawable.ic_fluent_person_swap_24_regular, this::onManageAccountsClick),
				new ListItem<>(R.string.log_out, 0, R.drawable.ic_fluent_sign_out_24_regular, this::onLogOutClick, R.attr.colorM3Error, false)
		));

		Instance instance=AccountSessionManager.getInstance().getInstanceInfo(account.domain);
		if(!instance.isAkkoma() && !instance.isIceshrimpJs()){ // hide filter settings on Akkoma and Iceshrimp-JS because the servers don't support the feature
			data.add(3, new ListItem<>(R.string.settings_filters, 0, R.drawable.ic_fluent_filter_24_regular, this::onFiltersClick));
		}

		if(BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.equals("appcenterPrivateBeta")){
			data.add(0, new ListItem<>("Debug settings", null, R.drawable.ic_fluent_wrench_screwdriver_24_regular, i->Nav.go(getActivity(), SettingsDebugFragment.class, makeFragmentArgs()), null, 0, true));
		}

		AccountSession session=AccountSessionManager.get(accountID);
		session.reloadPreferences(null);
		session.updateAccountInfo();
		E.register(this);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		E.unregister(this);
	}

	@Override
	protected void doLoadData(int offset, int count){}

	@Override
	protected void onHidden(){
		super.onHidden();
		if(!loggedOut)
			account.savePreferencesIfPending();
	}

	@Override
	protected RecyclerView.Adapter<?> getAdapter(){
		View banner=getActivity().getLayoutInflater().inflate(R.layout.item_settings_banner, list, false);
		updateText=banner.findViewById(R.id.text);
		TextView bannerTitle=banner.findViewById(R.id.title);
		ImageView bannerIcon=banner.findViewById(R.id.icon);
		updateButton1=banner.findViewById(R.id.button);
		updateButton2=banner.findViewById(R.id.button2);
		bannerAdapter=new HideableSingleViewRecyclerAdapter(banner);
		bannerAdapter.setVisible(false);
		updateButton1.setOnClickListener(this::onUpdateButtonClick);
		updateButton2.setOnClickListener(this::onUpdateButtonClick);

		bannerTitle.setText(R.string.app_update_ready);
		bannerIcon.setImageResource(R.drawable.ic_fluent_phone_update_24_regular);

		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		adapter.addAdapter(bannerAdapter);
		adapter.addAdapter(super.getAdapter());
		return adapter;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		if(GithubSelfUpdater.needSelfUpdating()){
			updateUpdateBanner();
		}
	}

	private Bundle makeFragmentArgs(){
		Bundle args=new Bundle();
		args.putString("account", accountID);
		return args;
	}

	private void onBehaviorClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsBehaviorFragment.class, makeFragmentArgs());
	}

	private void onDisplayClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsDisplayFragment.class, makeFragmentArgs());
	}

	private void onPrivacyClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsPrivacyFragment.class, makeFragmentArgs());
	}

	private void onFiltersClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsFiltersFragment.class, makeFragmentArgs());
	}

	private void onNotificationsClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsNotificationsFragment.class, makeFragmentArgs());
	}

	private void onInstanceClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsInstanceFragment.class, makeFragmentArgs());
	}

	private void onAboutClick(ListItem<?> item_){
		Nav.go(getActivity(), SettingsAboutAppFragment.class, makeFragmentArgs());
	}

	private void onManageAccountsClick(ListItem<?> item){
		new AccountSwitcherSheet(getActivity(), null).show();
	}

	private void onLogOutClick(ListItem<?> item_){
		AccountSession session=AccountSessionManager.getInstance().getAccount(accountID);
		new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.log_out)
				.setMessage(getString(R.string.confirm_log_out, session.getFullUsername()))
				.setPositiveButton(R.string.log_out, (dialog, which)->account.logOut(getActivity(), ()->{
					loggedOut=true;
					((MainActivity)getActivity()).restartActivity();
				}))
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	@Subscribe
	public void onSelfUpdateStateChanged(SelfUpdateStateChangedEvent ev){
		updateUpdateBanner();
	}

	private void updateUpdateBanner(){
		GithubSelfUpdater.UpdateState state=GithubSelfUpdater.getInstance().getState();
		if(state==GithubSelfUpdater.UpdateState.NO_UPDATE || state==GithubSelfUpdater.UpdateState.CHECKING){
			bannerAdapter.setVisible(false);
		}else{
			bannerAdapter.setVisible(true);
			updateText.setText(getString(R.string.app_update_version, GithubSelfUpdater.getInstance().getUpdateInfo().version));
			if(state==GithubSelfUpdater.UpdateState.UPDATE_AVAILABLE){
				updateButton2.setVisibility(View.GONE);
				updateButton1.setEnabled(true);
				updateButton1.setText(getString(R.string.download_update, UiUtils.formatFileSize(getActivity(), GithubSelfUpdater.getInstance().getUpdateInfo().size, true)));
			}else if(state==GithubSelfUpdater.UpdateState.DOWNLOADING){
				updateButton2.setVisibility(View.VISIBLE);
				updateButton2.setText(R.string.cancel);
				updateButton1.setEnabled(false);
				list.removeCallbacks(updateDownloadProgressUpdater);
				updateDownloadProgressUpdater.run();
			}else if(state==GithubSelfUpdater.UpdateState.DOWNLOADED){
				updateButton2.setVisibility(View.GONE);
				updateButton1.setEnabled(true);
				updateButton1.setText(R.string.install_update);
			}
		}
	}

	private void onUpdateButtonClick(View v){
		if(v.getId()==R.id.button){
			GithubSelfUpdater.UpdateState state=GithubSelfUpdater.getInstance().getState();
			if(state==GithubSelfUpdater.UpdateState.UPDATE_AVAILABLE){
				GithubSelfUpdater.getInstance().downloadUpdate();
			}else if(state==GithubSelfUpdater.UpdateState.DOWNLOADED){
				GithubSelfUpdater.getInstance().installUpdate(getActivity());
			}
		}else if(v.getId()==R.id.button2){
			GithubSelfUpdater.getInstance().cancelDownload();
		}
	}
}
