package net.seqular.network.model;

import com.google.gson.annotations.SerializedName;

import net.seqular.network.api.AllFieldsAreRequired;

import java.time.Instant;

@AllFieldsAreRequired
public class Marker extends BaseModel{
	public String lastReadId;
	public long version;
	public Instant updatedAt;

	@Override
	public String toString(){
		return "Marker{"+
				"lastReadId='"+lastReadId+'\''+
				", version="+version+
				", updatedAt="+updatedAt+
				'}';
	}

	public enum Type {
		@SerializedName("home")
		HOME,
		@SerializedName("notifications")
		NOTIFICATIONS
	}
}
