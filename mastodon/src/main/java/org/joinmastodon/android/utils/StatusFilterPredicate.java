package org.joinmastodon.android.utils;

import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.Status;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StatusFilterPredicate implements Predicate<Status>{
	private final List<Filter> filters;

	public StatusFilterPredicate(List<Filter> filters){
		this.filters=filters;
	}

	public StatusFilterPredicate(String accountID, Filter.FilterContext context){
		filters=AccountSessionManager.getInstance().getAccount(accountID).wordFilters.stream().filter(f->f.context.contains(context)).collect(Collectors.toList());
	}

	// TODO: rewrite (see testHasStatusWarning) and generalize
	@Override
	public boolean test(Status status){
		if(status.filtered!=null){
			if (status.filtered.isEmpty()){
				return true;
			}
			boolean matches=status.filtered.stream()
					.map(filterResult->filterResult.filter)
					.filter(filter->filter.expiresAt==null||filter.expiresAt.isAfter(Instant.now()))
					.anyMatch(filter->filter.filterAction==Filter.FilterAction.HIDE);
			return !matches;
		}
		for(Filter filter:filters){
			if(filter.matches(status))
				return false;
		}
		return true;
	}

	// TODO: move this method elsewhere; it's not part of the actual StatusFilterPredicate
	public boolean testHasStatusWarning(Status status, Filter.FilterContext context) {
		if (status.filtered != null) {
			// use server-provided info on whether this status was filtered
			if (status.filtered.isEmpty()) return false;
			return status.filtered.stream()
					.map(filterResult -> filterResult.filter)
					.filter(filter -> filter.expiresAt == null || filter.expiresAt.isAfter(Instant.now()))
					.filter(filter -> filter.context.contains(context))
					.anyMatch(filter -> filter.filterAction == Filter.FilterAction.WARN);
		} else {
			// look through local filters instead
			return filters.stream().anyMatch(filter -> filter.matches(status));
		}
	}
}
