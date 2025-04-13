package net.seqular.network.model;

import com.google.gson.annotations.SerializedName;

public enum FilterAction{
	@SerializedName("warn")
	WARN,
	@SerializedName("hide")
	HIDE
}
