package net.seqular.network.fragments.discover;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.seqular.network.R;
import net.seqular.network.api.requests.trends.GetTrendingHashtags;
import net.seqular.network.fragments.IsOnTop;
import net.seqular.network.fragments.ScrollableToTop;
import net.seqular.network.model.Hashtag;
import net.seqular.network.ui.utils.UiUtils;
import net.seqular.network.ui.views.HashtagChartView;
import net.seqular.network.utils.ProvidesAssistContent;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;

public class TrendingHashtagsFragment extends BaseRecyclerFragment<Hashtag> implements ScrollableToTop, IsOnTop, ProvidesAssistContent.ProvidesWebUri{
	private String accountID;

	public TrendingHashtagsFragment(){
		super(10);
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
		currentRequest=new GetTrendingHashtags(10)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Hashtag> result){
						onDataLoaded(result, false);
					}
				})
				.exec(accountID);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		return new HashtagsAdapter();
	}

	@Override
	public void scrollToTop(){
		smoothScrollRecyclerViewToTop(list);
	}

	@Override
	public boolean isOnTop(){
		return isRecyclerViewOnTop(list);
	}

	@Override
	public String getAccountID() {
		return accountID;
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		return isInstanceAkkoma() ? null : base.path("/explore/tags").build();
	}

	private class HashtagsAdapter extends RecyclerView.Adapter<HashtagViewHolder>{
		@NonNull
		@Override
		public HashtagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new HashtagViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull HashtagViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}
	}

	private class HashtagViewHolder extends BindableViewHolder<Hashtag> implements UsableRecyclerView.Clickable{
		private final TextView title, subtitle;
		private final HashtagChartView chart;

		public HashtagViewHolder(){
			super(getActivity(), R.layout.item_trending_hashtag, list);
			title=findViewById(R.id.title);
			subtitle=findViewById(R.id.subtitle);
			chart=findViewById(R.id.chart);
		}

		@Override
		public void onBind(Hashtag item){
			title.setText('#'+item.name);
			if (item.history == null || item.history.isEmpty()) {
				subtitle.setText(null);
				chart.setVisibility(View.GONE);
				return;
			}
			chart.setVisibility(View.VISIBLE);
			int numPeople=item.history.get(0).accounts;
			if(item.history.size()>1)
				numPeople+=item.history.get(1).accounts;
			subtitle.setText(getResources().getQuantityString(R.plurals.x_people_talking, numPeople, numPeople));
			chart.setData(item.history);
		}

		@Override
		public void onClick(){
			UiUtils.openHashtagTimeline(getActivity(), accountID, item);
		}
	}
}
