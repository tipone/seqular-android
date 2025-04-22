package net.seqular.network.model;

import net.seqular.network.api.RequiredField;
import org.parceler.Parcel;

@Parcel
public class ExtendedDescription extends BaseModel{
	@RequiredField
	public String content;
	public String updatedAt;

	@Override
	public String toString() {
		return "ExtendedDescription{" +
				"content='" + content + '\'' +
				", updatedAt='" + updatedAt + '\'' +
				'}';
	}
}
