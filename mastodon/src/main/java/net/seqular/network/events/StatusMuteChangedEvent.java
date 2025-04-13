package net.seqular.network.events;

import net.seqular.network.model.Status;

public class StatusMuteChangedEvent{
	public String id;
	public boolean muted;
	public Status status;

	public StatusMuteChangedEvent(Status s){
		id=s.id;
		muted=s.muted;
		status=s;
	}
}
