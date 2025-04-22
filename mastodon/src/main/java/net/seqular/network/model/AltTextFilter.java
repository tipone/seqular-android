package net.seqular.network.model;

import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.MastodonApp;
import net.seqular.network.R;
import org.jsoup.internal.StringUtil;

import java.util.EnumSet;

public class AltTextFilter extends LegacyFilter {

	public AltTextFilter(FilterAction filterAction, EnumSet<FilterContext> filterContexts) {
		this.filterAction=filterAction;
		this.title=MastodonApp.context.getString(R.string.sk_no_alt_text);
		this.isRemote=false;
		this.context=filterContexts;
	}

	@Override
	public boolean matches(Status status) {
		return status.getContentStatus().mediaAttachments.stream().map(attachment -> attachment.description).anyMatch(StringUtil::isBlank);
	}

	@Override
	public boolean isActive(){
		return !GlobalUserPreferences.showPostsWithoutAlt;
	}
}
