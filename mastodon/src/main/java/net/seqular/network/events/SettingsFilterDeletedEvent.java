package net.seqular.network.events;

public class SettingsFilterDeletedEvent{
	public final String accountID;
	public final String filterID;

	public SettingsFilterDeletedEvent(String accountID, String filterID){
		this.accountID=accountID;
		this.filterID=filterID;
	}
}
