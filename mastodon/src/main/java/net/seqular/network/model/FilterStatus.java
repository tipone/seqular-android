package net.seqular.network.model;

import net.seqular.network.api.AllFieldsAreRequired;
import org.parceler.Parcel;

@AllFieldsAreRequired
@Parcel
public class FilterStatus extends BaseModel{
	public String id;
	public String statusId;
}
