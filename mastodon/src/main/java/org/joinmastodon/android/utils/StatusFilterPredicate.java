package org.joinmastodon.android.utils;

import static org.joinmastodon.android.model.Filter.FilterAction.HIDE;
import static org.joinmastodon.android.model.Filter.FilterAction.WARN;
import static org.joinmastodon.android.model.Filter.FilterContext.ACCOUNT;
import static org.joinmastodon.android.model.Filter.FilterContext.HOME;
import static org.joinmastodon.android.model.Filter.FilterContext.NOTIFICATIONS;
import static org.joinmastodon.android.model.Filter.FilterContext.PUBLIC;
import static org.joinmastodon.android.model.Filter.FilterContext.THREAD;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.AltTextFilter;
import org.joinmastodon.android.model.Filter;
import org.joinmastodon.android.model.Status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatusFilterPredicate implements Predicate<Status> {

	//Hide in timelines and warn in threads
	private final List<Filter> clientFilters;
	private final List<Filter> filters;
	private final Filter.FilterContext context;
	private final Filter.FilterAction action;
	private Filter applyingFilter;

	/**
	 * @param context null makes the predicate pass automatically
	 * @param action  defines what the predicate should check:
	 *                status should not be hidden or should not display with warning
	 */
	public StatusFilterPredicate(List<Filter> filters, Filter.FilterContext context, Filter.FilterAction action) {
		this.filters = filters;
		this.context = context;
		this.action = action;
		this.clientFilters = GlobalUserPreferences.showPostsWithoutAlt ? List.of()
				: List.of(new AltTextFilter(HIDE, HOME, PUBLIC, ACCOUNT), new AltTextFilter(WARN, THREAD, NOTIFICATIONS));
	}

	public StatusFilterPredicate(List<Filter> filters, Filter.FilterContext context) {
		this(filters, context, HIDE);
	}

	/**
	 * @param context null makes the predicate pass automatically
	 * @param action  defines what the predicate should check:
	 *                status should not be hidden or should not display with warning
	 */
	public StatusFilterPredicate(String accountID, Filter.FilterContext context, Filter.FilterAction action) {
		this(AccountSessionManager.getInstance().getAccount(accountID).wordFilters.stream().filter(f -> f.context.contains(context)).collect(Collectors.toList()),
				context,
				action);
	}

	/**
	 * @param context null makes the predicate pass automatically
	 */
	public StatusFilterPredicate(String accountID, Filter.FilterContext context) {
		this(accountID, context, HIDE);
	}

	/**
	 * @return whether the status should be displayed without being hidden/warned about.
	 * will always return true if the context is null.
	 * true = display this status,
	 * false = filter this status
	 */
	@Override
	public boolean test(Status status) {
		if (context == null) return true;

		Stream<Filter> matchingFilters = status.filtered != null
				// use server-provided per-status info (status.filtered) if available
				? status.filtered.stream().map(f -> f.filter)
				// or fall back to cached filters
				: filters.stream().filter(filter -> filter.matches(status));

		Optional<Filter> applyingFilter = matchingFilters
				// discard expired filters
				.filter(filter -> filter.expiresAt == null || filter.expiresAt.isAfter(Instant.now()))
				// only apply filters for given context
				.filter(filter -> filter.context.contains(context))
				// treating filterAction = null (from filters list) as FilterAction.HIDE
				.filter(filter -> filter.filterAction == null ? action == HIDE : filter.filterAction == action)
				.findAny();

		//Apply client filters if no server filter is triggered
		if (applyingFilter.isEmpty()) {
			applyingFilter = clientFilters.stream()
					.filter(filter -> filter.context.contains(context))
					.filter(filter -> filter.filterAction == null ? action == HIDE : filter.filterAction == action)
					.filter(filter -> filter.matches(status))
					.findAny();
		}

		this.applyingFilter = applyingFilter.orElse(null);
		return applyingFilter.isEmpty();
	}

	public Filter getApplyingFilter() {
		return applyingFilter;
	}
}
