package net.seqular.network.events;

import net.seqular.network.model.FollowList;

public class ListUpdatedCreatedEvent {
	public final String id;
	public final String title;
	public final FollowList.RepliesPolicy repliesPolicy;
	public final boolean exclusive;

	public ListUpdatedCreatedEvent(String id, String title, boolean exclusive, FollowList.RepliesPolicy repliesPolicy) {
		this.id = id;
		this.title = title;
		this.exclusive = exclusive;
		this.repliesPolicy = repliesPolicy;
	}
}
