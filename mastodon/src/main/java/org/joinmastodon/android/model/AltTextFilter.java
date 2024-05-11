package org.joinmastodon.android.model;

import org.joinmastodon.android.GlobalUserPreferences;
import org.jsoup.internal.StringUtil;

import java.util.EnumSet;

public class AltTextFilter extends LegacyFilter {

	public AltTextFilter(FilterAction filterAction, EnumSet<FilterContext> filterContexts) {
		this.filterAction = filterAction;
		isRemote = false;
		context = filterContexts;
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
