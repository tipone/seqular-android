package net.seqular.network.fragments.report;

import android.app.Activity;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import net.seqular.network.E;
import net.seqular.network.R;
import net.seqular.network.api.requests.accounts.GetAccountStatuses;
import net.seqular.network.events.FinishReportFragmentsEvent;
import net.seqular.network.fragments.StatusListFragment;
import net.seqular.network.model.Account;
import net.seqular.network.model.FilterContext;
import net.seqular.network.model.Status;
import net.seqular.network.ui.OutlineProviders;
import net.seqular.network.ui.displayitems.AudioStatusDisplayItem;
import net.seqular.network.ui.displayitems.CheckableHeaderStatusDisplayItem;
import net.seqular.network.ui.displayitems.DummyStatusDisplayItem;
import net.seqular.network.ui.displayitems.LinkCardStatusDisplayItem;
import net.seqular.network.ui.displayitems.MediaGridStatusDisplayItem;
import net.seqular.network.ui.displayitems.StatusDisplayItem;
import net.seqular.network.ui.utils.UiUtils;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.MergeRecyclerAdapter;
import me.grishka.appkit.utils.SingleViewRecyclerAdapter;
import me.grishka.appkit.utils.V;

public class ReportAddPostsChoiceFragment extends StatusListFragment{
	private Button btn;
	private View buttonBar;
	private ArrayList<String> selectedIDs=new ArrayList<>();
	private String accountID;
	private Account reportAccount;
	private Status reportStatus;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setListLayoutId(R.layout.fragment_content_report_posts);
		setLayout(R.layout.fragment_report_posts);
		E.register(this);
	}

	@Override
	public void onDestroy(){
		E.unregister(this);
		super.onDestroy();
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		accountID=getArguments().getString("account");
		reportAccount=Parcels.unwrap(getArguments().getParcelable("reportAccount"));
		reportStatus=Parcels.unwrap(getArguments().getParcelable("status"));
		if(reportStatus!=null){
			selectedIDs.add(reportStatus.id);
			setTitle(R.string.report_title_post);
		}else{
			setTitle(getString(R.string.report_title, reportAccount.acct));
		}
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetAccountStatuses(reportAccount.id, getMaxID(), null, count, GetAccountStatuses.Filter.OWN_POSTS_AND_REPLIES)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Status> result){
						if(getActivity()==null) return;
						for(Status s:result){
							s.sensitive=true;
						}
						onDataLoaded(result, !result.isEmpty());
					}
				})
				.exec(accountID);
	}

	public void onToggleItem(String id){
		if(selectedIDs.contains(id))
			selectedIDs.remove(id);
		else
			selectedIDs.add(id);
		CheckableHeaderStatusDisplayItem.Holder holder=findHolderOfType(id, CheckableHeaderStatusDisplayItem.Holder.class);
		if(holder!=null) holder.rebind();
		else notifyItemChanged(id, CheckableHeaderStatusDisplayItem.class);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		btn=view.findViewById(R.id.btn_next);
		btn.setOnClickListener(this::onButtonClick);
		buttonBar=view.findViewById(R.id.button_bar);

		list.addItemDecoration(new RecyclerView.ItemDecoration(){
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
				RecyclerView.ViewHolder holder=parent.getChildViewHolder(view);
				if(holder.getAbsoluteAdapterPosition()==0 || holder instanceof CheckableHeaderStatusDisplayItem.Holder)
					return;
				boolean isRTL=parent.getLayoutDirection()==View.LAYOUT_DIRECTION_RTL;
				if(isRTL) outRect.right=V.dp(40);
				else outRect.left=V.dp(40);
				if(holder instanceof AudioStatusDisplayItem.Holder){
					outRect.bottom=V.dp(16);
				}else if(holder instanceof LinkCardStatusDisplayItem.Holder || holder instanceof MediaGridStatusDisplayItem.Holder){
					outRect.bottom=V.dp(8);
					if(isRTL){
						outRect.right+=V.dp(16);
						outRect.left=V.dp(16);
					}else{
						outRect.left+=V.dp(16);
						outRect.right=V.dp(16);
					}
				}
			}
		});

		ProgressBar topProgress=view.findViewById(R.id.top_progress);
		topProgress.setProgress(getArguments().containsKey("ruleIDs") ? 50 : 33);
	}

	@Override
	protected int getMainAdapterOffset(){
		return 1;
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		View headerView=getActivity().getLayoutInflater().inflate(R.layout.item_list_header, list, false);
		TextView title=headerView.findViewById(R.id.title);
		TextView subtitle=headerView.findViewById(R.id.subtitle);
		title.setText(R.string.report_choose_posts);
		subtitle.setText(R.string.report_choose_posts_subtitle);

		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		adapter.addAdapter(new SingleViewRecyclerAdapter(headerView));
		adapter.addAdapter(super.getAdapter());
		return adapter;
	}

	private void onButtonClick(View v){
		Bundle args=new Bundle();
		args.putString("account", accountID);
		args.putParcelable("reportAccount", Parcels.wrap(reportAccount));
		if(reportStatus!=null)
			args.putBoolean("fromPost", true);
		if(v.getId()==R.id.btn_next){
			args.putStringArrayList("statusIDs", selectedIDs);
		}else{
			ArrayList<String> ids=new ArrayList<>();
			if(reportStatus!=null)
				ids.add(reportStatus.id);
			args.putStringArrayList("statusIDs", ids);
		}
		args.putStringArrayList("ruleIDs", getArguments().getStringArrayList("ruleIDs"));
		args.putString("reason", getArguments().getString("reason"));
		args.putParcelable("relationship", getArguments().getParcelable("relationship"));
		Nav.go(getActivity(), ReportCommentFragment.class, args);
	}

	@Override
	public void onApplyWindowInsets(WindowInsets insets){
		super.onApplyWindowInsets(UiUtils.applyBottomInsetToFixedView(buttonBar, insets));
	}

	@Subscribe
	public void onFinishReportFragments(FinishReportFragmentsEvent ev){
		if(ev.reportAccountID.equals(reportAccount.id))
			Nav.finish(this);
	}

	@Override
	protected boolean wantsOverlaySystemNavigation(){
		return false;
	}

	@Override
	protected boolean wantsElevationOnScrollEffect(){
		return false;
	}

	@Override
	protected List<StatusDisplayItem> buildDisplayItems(Status s){
		List<StatusDisplayItem> items=StatusDisplayItem.buildItems(this, s, accountID, s, knownAccounts, getFilterContext(), StatusDisplayItem.FLAG_NO_FOOTER | StatusDisplayItem.FLAG_CHECKABLE | StatusDisplayItem.FLAG_MEDIA_FORCE_HIDDEN);
		items.add(new DummyStatusDisplayItem(s.getID(), this));
		return items;
	}

	@Override
	protected FilterContext getFilterContext(){
		return null;
	}

	@Override
	protected void onModifyItemViewHolder(BindableViewHolder<StatusDisplayItem> holder){
		if((Object)holder instanceof MediaGridStatusDisplayItem.Holder h){
			View layout=h.getLayout();
			layout.setOutlineProvider(OutlineProviders.roundedRect(8));
			layout.setClipToOutline(true);
			View overlay=h.getSensitiveOverlay();
			overlay.setOutlineProvider(OutlineProviders.roundedRect(8));
			overlay.setClipToOutline(true);
		}else if((Object)holder instanceof CheckableHeaderStatusDisplayItem.Holder h){
			h.setIsChecked(this::isChecked);
		}
	}

	private boolean isChecked(CheckableHeaderStatusDisplayItem.Holder holder){
		return selectedIDs.contains(holder.getItem().parentID);
	}

	@Override
	public Uri getWebUri(Uri.Builder base){
		return null;
	}
}
