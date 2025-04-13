package net.seqular.network.api.requests.catalog;

import android.net.Uri;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.catalog.CatalogDefaultInstance;

import java.util.List;

public class GetCatalogDefaultInstances extends MastodonAPIRequest<List<CatalogDefaultInstance>>{
	public GetCatalogDefaultInstances(){
		super(HttpMethod.GET, null, new TypeToken<>(){});
		setTimeout(500);
	}

	@Override
	public Uri getURL(){
		return Uri.parse("https://api.joinmastodon.org/default-servers");
	}
}
