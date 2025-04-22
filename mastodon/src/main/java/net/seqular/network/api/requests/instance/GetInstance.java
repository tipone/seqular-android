package net.seqular.network.api.requests.instance;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.Instance;

public class GetInstance extends MastodonAPIRequest<Instance>{
	public GetInstance(){
		super(HttpMethod.GET, "/instance", Instance.class);
	}

	public static class V2 extends MastodonAPIRequest<Instance.V2>{
		public V2(){
			super(HttpMethod.GET, "/instance", Instance.V2.class);
		}

		@Override
		protected String getPathPrefix() {
			return "/api/v2";
		}
	}
}
