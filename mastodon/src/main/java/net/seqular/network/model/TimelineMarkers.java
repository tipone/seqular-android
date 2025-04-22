package net.seqular.network.model;

public class TimelineMarkers{
	public Marker home, notifications;

	@Override
	public String toString(){
		return "TimelineMarkers{"+
				"home="+home+
				", notifications="+notifications+
				'}';
	}
}
