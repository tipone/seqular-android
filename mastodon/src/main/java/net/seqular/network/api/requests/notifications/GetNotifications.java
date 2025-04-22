package net.seqular.network.api.requests.notifications;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.ApiUtils;
import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Notification;

import java.util.EnumSet;
import java.util.List;

public class GetNotifications extends MastodonAPIRequest<List<Notification>>{
	public GetNotifications(String maxID, int limit, EnumSet<Notification.Type> includeTypes, boolean isPleromaInstance){
		super(HttpMethod.GET, "/notifications", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(limit>0)
			addQueryParameter("limit", ""+limit);
		if(includeTypes!=null){
			if(!isPleromaInstance) {
				for(String type:ApiUtils.enumSetToStrings(includeTypes, Notification.Type.class)){
					addQueryParameter("types[]", type);
				}
				for(String type:ApiUtils.enumSetToStrings(EnumSet.complementOf(includeTypes), Notification.Type.class)){
					addQueryParameter("exclude_types[]", type);
				}
			}else{
				for(String type:ApiUtils.enumSetToStrings(includeTypes, Notification.Type.class)){
					addQueryParameter("include_types[]", type);
				}
			}
		}
		removeUnsupportedItems=true;
	}
}
