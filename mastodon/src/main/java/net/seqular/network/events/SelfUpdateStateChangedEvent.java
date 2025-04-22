package net.seqular.network.events;

import net.seqular.network.updater.GithubSelfUpdater;

public class SelfUpdateStateChangedEvent{
	public final GithubSelfUpdater.UpdateState state;

	public SelfUpdateStateChangedEvent(GithubSelfUpdater.UpdateState state){
		this.state=state;
	}
}
