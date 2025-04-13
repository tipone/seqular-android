package net.seqular.network.model;

import android.util.Patterns;

import androidx.annotation.NonNull;

import net.seqular.network.api.ObjectValidationException;
import net.seqular.network.api.RequiredField;
import net.seqular.network.api.session.AccountSession;
import net.seqular.network.api.session.AccountSessionManager;
import net.seqular.network.model.Poll.Option;
import org.parceler.Parcel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Parcel
public class ScheduledStatus extends BaseModel implements DisplayItemsParent{
	private static final Pattern HIGHLIGHT_PATTER=Pattern.compile("(?<!\\w)(?:@([a-z0-9_]+)(@[a-z0-9_\\.\\-]*)?|#([^\\s.]+)|:([a-z0-9_]+))|" +Patterns.WEB_URL, Pattern.CASE_INSENSITIVE);

    @RequiredField
    public String id;
    @RequiredField
    public Instant scheduledAt;
    @RequiredField
    public Params params;
    public List<Attachment> mediaAttachments;

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void postprocess() throws ObjectValidationException {
        super.postprocess();
        if(mediaAttachments==null) mediaAttachments=List.of();
        for(Attachment a:mediaAttachments)
            a.postprocess();
        if(params!=null) params.postprocess();
    }

    @Parcel
    public static class Params extends BaseModel {
        @RequiredField
        public String text;
        public String spoilerText;
        @RequiredField
        public StatusPrivacy visibility;
        public long inReplyToId;
        public ScheduledPoll poll;
        public boolean sensitive;
        public boolean withRateLimit;
        public String language;
        public String idempotency;
        public String applicationId;
        public List<String> mediaIds;
        public ContentType contentType;

        @Override
        public void postprocess() throws ObjectValidationException {
            super.postprocess();
            if(poll!=null) poll.postprocess();
        }
    }

    @Parcel
    public static class ScheduledPoll extends BaseModel {
        @RequiredField
        public String expiresIn;
        @RequiredField
        public List<String> options;
        public boolean multiple;
        public boolean hideTotals;

        public Poll toPoll() {
            Poll p=new Poll();
            p.voted=true;
            p.emojis=List.of();
            p.ownVotes=List.of();
            p.multiple=multiple;
            p.options=options.stream().map(Option::new).collect(Collectors.toList());
			p.expiresAt=Instant.now().plus(Integer.parseInt(expiresIn)+1, ChronoUnit.SECONDS);
            return p;
        }
    }

    public Status toStatus() {
        Status s=Status.ofFake(id, params.text, scheduledAt);
        s.mediaAttachments=mediaAttachments;
        s.inReplyToId=params.inReplyToId>0 ? ""+params.inReplyToId : null;
        s.spoilerText=params.spoilerText;
        s.visibility=params.visibility;
        s.language=params.language;
        s.sensitive=params.sensitive;
		// hide media preview only if status is marked as sensitive
		s.sensitiveRevealed=!params.sensitive;
        if(params.poll!=null) s.poll=params.poll.toPoll();
        return s;
    }

	/**
	 * Creates a fake status, which has (somewhat) correctly formatted mentions, hashtags and URLs.
	 *
	 * @param accountID the ID of the account
	 * @return the formatted Status object
	 */
	public Status toFormattedStatus(String accountID){
		AccountSession self=AccountSessionManager.get(accountID);
		Status s=this.toStatus();
		// the mastodon api does not return formatted (html) content, only the raw content, so we modify it
		s.content=s.content.replace("\n", "<br>");
		if(!s.content.contains("@") && !s.content.contains("#") && !s.content.contains(":"))
			return s;

		StringBuffer sb=new StringBuffer();
		Matcher matcher=HIGHLIGHT_PATTER.matcher(s.content);

		// I'm sure this will cause problems at some point...
		while(matcher.find()){
			String content=matcher.group();
			String href="";
			// add relevant links, so on-click actions work
			// hashtags are done by the parser
			if(content.startsWith("@"))
				href=" href=\""+formatMention(content, self.domain)+"\" class=\"u-url mention\"";
			else if(content.startsWith("https://"))
				href=" href=\""+content+"\"";

			matcher.appendReplacement(sb, "<a"+href+">"+content+"</a>");
		}
		matcher.appendTail(sb);
		s.content=sb.toString();
		return s;
	}

	/**
	 * Converts a string mention into a URL of the account.
	 * @param mention Mention in the form a of user name with an optional instance URL
	 * @param instanceURL URL of the home instance of the user
	 * @return Formatted HTML or the mention
	 */
	@NonNull
	private static String formatMention(@NonNull String mention, @NonNull String instanceURL){
		String[] parts=mention.split("@");
		if(parts.length>1){
			String username=parts[1];
			String domain=parts.length==3 ? parts[2] : instanceURL;
			return "https://"+domain+"/@"+username;
		}
		return mention;
	}
}
