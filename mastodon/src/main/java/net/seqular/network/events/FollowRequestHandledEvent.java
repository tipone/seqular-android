package net.seqular.network.events;

import net.seqular.network.model.Account;
import net.seqular.network.model.Relationship;

public class FollowRequestHandledEvent {
	public String accountID;
	public boolean accepted;
	public Account account;
	public Relationship	relationship;

	public FollowRequestHandledEvent(String accountID, boolean accepted, Account account, Relationship rel){
		this.accountID=accountID;
		this.accepted=accepted;
		this.account=account;
		this.relationship=rel;
	}
}
