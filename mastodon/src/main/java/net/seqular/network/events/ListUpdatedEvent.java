package net.seqular.network.events;

import net.seqular.network.model.FollowList;

public class ListUpdatedEvent{
	public final String accountID;
	public final FollowList list;

	public ListUpdatedEvent(String accountID, FollowList list){
		this.accountID=accountID;
		this.list=list;
	}
}
