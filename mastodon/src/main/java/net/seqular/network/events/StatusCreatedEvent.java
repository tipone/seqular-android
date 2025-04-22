package net.seqular.network.events;

import net.seqular.network.model.Status;

public class StatusCreatedEvent{
	public final Status status;
	public final String accountID;

	public StatusCreatedEvent(Status status, String accountID){
		this.status=status;
		this.accountID=accountID;
		status.fromStatusCreated=true;
	}
}
