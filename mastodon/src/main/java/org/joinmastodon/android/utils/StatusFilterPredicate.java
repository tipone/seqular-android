package org.joinmastodon.android.utils;

import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.Status;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatusFilterPredicate implements Predicate<Status>{
	private final List<Filter> filters;
	private final Filter.FilterContext context;
	private final Filter.FilterAction action;

	/**
	 * @param context null makes the predicate pass automatically
	 * @param action defines what the predicate should check:
	 * 	             status should not be hidden or should not display with warning
	 */
	public StatusFilterPredicate(List<Filter> filters, Filter.FilterContext context, Filter.FilterAction action){
		this.filters = filters;
		this.context = context;
		this.action = action;
	}

	public StatusFilterPredicate(List<Filter> filters, Filter.FilterContext context){
		this(filters, context, Filter.FilterAction.HIDE);
	}

	/**
	 * @param context null makes the predicate pass automatically
	 * @param action defines what the predicate should check:
	 *               status should not be hidden or should not display with warning
	 */
	public StatusFilterPredicate(String accountID, Filter.FilterContext context, Filter.FilterAction action){
		filters=AccountSessionManager.getInstance().getAccount(accountID).wordFilters.stream().filter(f->f.context.contains(context)).collect(Collectors.toList());
		this.context = context;
		this.action = action;
	}

	/**
	 * @param context null makes the predicate pass automatically
	 */
	public StatusFilterPredicate(String accountID, Filter.FilterContext context){
		this(accountID, context, Filter.FilterAction.HIDE);
	}

	/**
	 * @return whether the status should be displayed without being hidden/warned about.
	 *         will always return true if the context is null.
	 *         true = display this status,
	 *         false = filter this status
	 */
	@Override
	public boolean test(Status status){
		if (context == null) return true;

		Stream<Filter> stream = status.filtered != null
				// use server-provided per-status info (status.filtered) if available
				? status.filtered.stream().map(f -> f.filter)
				// or fall back to cached filters
				: filters.stream().filter(filter -> filter.matches(status));

		return stream
				// discard expired filters
				.filter(filter -> filter.expiresAt == null || filter.expiresAt.isAfter(Instant.now()))
				// only apply filters for given context
				.filter(filter -> filter.context.contains(context))
				// treating filterAction = null (from filters list) as FilterAction.HIDE
				.noneMatch(filter -> filter.filterAction == null ? action == Filter.FilterAction.HIDE : filter.filterAction == action);
	}
}
