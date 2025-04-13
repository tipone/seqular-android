package net.seqular.network.model;

import net.seqular.network.api.AllFieldsAreRequired;
import net.seqular.network.api.ObjectValidationException;

import java.util.List;

@AllFieldsAreRequired
public class StatusContext extends BaseModel{
	public List<Status> ancestors;
	public List<Status> descendants;

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		for(Status s:ancestors)
			s.postprocess();
		for(Status s:descendants)
			s.postprocess();
	}
}
