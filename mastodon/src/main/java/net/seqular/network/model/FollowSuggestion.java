package net.seqular.network.model;

import net.seqular.network.api.ObjectValidationException;
import net.seqular.network.api.RequiredField;

public class FollowSuggestion extends BaseModel{
	@RequiredField
	public Account account;
//	public String source;

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		account.postprocess();
	}
}
