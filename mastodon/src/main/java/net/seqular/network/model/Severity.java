package net.seqular.network.model;

import com.google.gson.annotations.SerializedName;

public enum Severity {
	@SerializedName("silence")
	SILENCE,
	@SerializedName("suspend")
	SUSPEND
}