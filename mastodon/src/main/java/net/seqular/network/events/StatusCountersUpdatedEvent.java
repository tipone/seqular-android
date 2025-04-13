package net.seqular.network.events;

import net.seqular.network.model.Status;

public class StatusCountersUpdatedEvent{
	public String id;
	public long favorites, reblogs, replies;
	public boolean favorited, reblogged, bookmarked, pinned;
	public Status status;

	public StatusCountersUpdatedEvent(Status s){
		id=s.id;
		status=s;
		favorites=s.favouritesCount;
		reblogs=s.reblogsCount;
		replies=s.repliesCount;
		favorited=s.favourited;
		reblogged=s.reblogged;
		bookmarked=s.bookmarked;
		pinned=s.pinned;
	}
}
