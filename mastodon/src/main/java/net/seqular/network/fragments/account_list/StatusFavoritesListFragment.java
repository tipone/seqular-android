package net.seqular.network.fragments.account_list;

import android.net.Uri;
import android.os.Bundle;

import net.seqular.network.R;
import net.seqular.network.api.requests.HeaderPaginationRequest;
import net.seqular.network.api.requests.statuses.GetStatusFavorites;
import net.seqular.network.model.Account;
import net.seqular.network.model.Status;

public class StatusFavoritesListFragment extends StatusRelatedAccountListFragment{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		updateTitle(status);
	}

	@Override
	protected void updateTitle(Status status) {
		setTitle(getResources().getQuantityString(R.plurals.x_favorites, (int)(status.favouritesCount%1000), status.favouritesCount));
	}

	@Override
	public HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count){
		return new GetStatusFavorites(getCurrentInfo().id, maxID, count);
	}

	@Override
	public Uri getWebUri(Uri.Builder base) {
		Uri statusUri = super.getWebUri(base);
		return isInstanceAkkoma()
				? statusUri
				: statusUri.buildUpon().appendPath("favourites").build();
	}
}
