package net.seqular.network.model;

import net.seqular.network.api.ObjectValidationException;
import net.seqular.network.api.RequiredField;
import org.parceler.Parcel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Parcel
public class Filter extends BaseModel{
	@RequiredField
	public String id;

	@RequiredField
	public String title;

	@RequiredField
	public EnumSet<FilterContext> context;

	public Instant expiresAt;
	public FilterAction filterAction;

	public List<FilterKeyword> keywords=new ArrayList<>();

	public List<FilterStatus> statuses=new ArrayList<>();

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		for(FilterKeyword keyword:keywords)
			keyword.postprocess();
		for(FilterStatus status:statuses)
			status.postprocess();
	}

	public boolean isActive(){
		return expiresAt==null || expiresAt.isAfter(Instant.now());
	}

	@Override
	public String toString(){
		return "Filter{"+
				"id='"+id+'\''+
				", title='"+title+'\''+
				", context="+context+
				", expiresAt="+expiresAt+
				", filterAction="+filterAction+
				", keywords="+keywords+
				", statuses="+statuses+
				'}';
	}
}
