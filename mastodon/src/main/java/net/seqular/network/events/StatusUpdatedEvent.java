package net.seqular.network.events;

import net.seqular.network.model.Status;

public class StatusUpdatedEvent{
	public Status status;

	public StatusUpdatedEvent(Status status){
		this.status=status;
	}
}
