package net.seqular.network.api.requests.instance;

import com.google.gson.reflect.TypeToken;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.WeeklyActivity;

import java.util.List;

public class GetWeeklyActivity extends MastodonAPIRequest<List<WeeklyActivity>>{
	public GetWeeklyActivity(){
		super(HttpMethod.GET, "/instance/activity", new TypeToken<>(){});
	}

}
