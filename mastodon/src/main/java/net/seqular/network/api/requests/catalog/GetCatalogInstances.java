package net.seqular.network.api.requests.catalog;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.catalog.CatalogInstance;

import java.util.List;

public class GetCatalogInstances extends MastodonAPIRequest<List<CatalogInstance>>{

	private String lang, category;

	public GetCatalogInstances(String lang, String category){
		super(HttpMethod.GET, null, new TypeToken<>(){});
		this.lang=lang;
		this.category=category;
	}

	@Override
	public Uri getURL(){
		Uri.Builder builder=new Uri.Builder()
				.scheme("https")
				.authority("api.joinmastodon.org")
				.path("/servers");
		if(!TextUtils.isEmpty(lang))
			builder.appendQueryParameter("language", lang);
		if(!TextUtils.isEmpty(category))
			builder.appendQueryParameter("category", category);
		return builder.build();
	}
}
