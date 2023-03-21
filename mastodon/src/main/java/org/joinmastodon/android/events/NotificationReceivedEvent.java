package org.joinmastodon.android.events;

public class NotificationReceivedEvent {
	public String account, id;
	public NotificationReceivedEvent(String account, String id) {
		this.account = account;
		this.id = id;
	}
}
