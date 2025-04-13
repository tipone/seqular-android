package net.seqular.network.api.requests.statuses;

import net.seqular.network.api.MastodonAPIRequest;
import net.seqular.network.model.ContentType;
import net.seqular.network.model.ScheduledStatus;
import net.seqular.network.model.Status;
import net.seqular.network.model.StatusPrivacy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CreateStatus extends MastodonAPIRequest<Status>{
	public static long EPOCH_OF_THE_YEAR_FIVE_THOUSAND=95617584000000L;
	public static final Instant DRAFTS_AFTER_INSTANT=Instant.ofEpochMilli(EPOCH_OF_THE_YEAR_FIVE_THOUSAND - 1) /* end of 4999 */;

	public static Instant getDraftInstant() {
		return DRAFTS_AFTER_INSTANT.plusMillis(System.currentTimeMillis());
	}

	public CreateStatus(CreateStatus.Request req, String uuid){
		super(HttpMethod.POST, "/statuses", Status.class);
		setRequestBody(req);
		addHeader("Idempotency-Key", uuid);
	}

	public static class Scheduled extends MastodonAPIRequest<ScheduledStatus>{
		public Scheduled(CreateStatus.Request req, String uuid){
			super(HttpMethod.POST, "/statuses", ScheduledStatus.class);
			setRequestBody(req);
			addHeader("Idempotency-Key", uuid);
		}
	}

	public static class Request{
		public String status;
		public List<MediaAttribute> mediaAttributes;
		public List<String> mediaIds;
		public Poll poll;
		public String inReplyToId;
		public boolean sensitive;
		public boolean localOnly;
		public String spoilerText;
		public StatusPrivacy visibility;
		public Instant scheduledAt;
		public String language;

		public String quoteId;
		public ContentType contentType;

		public boolean preview;

		public static class Poll{
			public ArrayList<String> options=new ArrayList<>();
			public int expiresIn;
			public boolean multiple;
			public boolean hideTotals;
		}

		public static class MediaAttribute{
			public String id;
			public String description;
			public String focus;

			public MediaAttribute(String id, String description, String focus){
				this.id=id;
				this.description=description;
				this.focus=focus;
			}
		}
	}
}
