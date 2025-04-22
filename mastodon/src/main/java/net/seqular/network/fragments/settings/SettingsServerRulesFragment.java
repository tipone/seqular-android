package net.seqular.network.fragments.settings;

import android.os.Bundle;

import net.seqular.network.api.requests.instance.GetInstance;
import net.seqular.network.fragments.MastodonRecyclerFragment;
import net.seqular.network.model.Instance;
import net.seqular.network.ui.adapters.InstanceRulesAdapter;
import org.parceler.Parcels;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;

public class SettingsServerRulesFragment extends MastodonRecyclerFragment<Instance.Rule>{
	private String accountID;
	private String domain;

	public SettingsServerRulesFragment(){
		super(20);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		accountID=getArguments().getString("account");
		domain=getArguments().getString("domain");
		Instance instance=Parcels.unwrap(getArguments().getParcelable("instance"));
		onDataLoaded(instance.rules);
		setRefreshEnabled(false);
	}

	@Override
	protected void doLoadData(int offset, int count){
		new GetInstance().setCallback(new Callback<>(){
			@Override
			public void onSuccess(Instance instance){
				onDataLoaded(instance.rules);
			}

			@Override
			public void onError(ErrorResponse error){
				error.showToast(getContext());
			}
		}).execRemote(domain);
	}

	@Override
	protected RecyclerView.Adapter<?> getAdapter(){
		return new InstanceRulesAdapter(data);
	}

	public RecyclerView getList(){
		return list;
	}
}
