package org.joinmastodon.android.model;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.R;
import org.jsoup.internal.StringUtil;

import java.util.EnumSet;

public class AltTextFilter extends Filter {

	public AltTextFilter(FilterAction filterAction, FilterContext firstContext, FilterContext... restContexts) {
		this.filterAction = filterAction;
		isRemote = false;
		context = EnumSet.of(firstContext, restContexts);
	}

	@Override
	public boolean matches(Status status) {
		return !GlobalUserPreferences.showPostsWithoutAlt && status.getContentStatus().mediaAttachments.stream().map(attachment -> attachment.description).anyMatch(StringUtil::isBlank);
	}
}
