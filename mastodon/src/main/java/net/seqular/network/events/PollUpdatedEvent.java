package net.seqular.network.events;

import net.seqular.network.model.Poll;

public class PollUpdatedEvent{
	public String accountID;
	public Poll poll;

	public PollUpdatedEvent(String accountID, Poll poll){
		this.accountID=accountID;
		this.poll=poll;
	}
}
