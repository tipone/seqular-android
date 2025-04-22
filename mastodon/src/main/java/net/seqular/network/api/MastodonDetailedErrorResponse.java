package net.seqular.network.api;

import java.util.List;
import java.util.Map;

public class MastodonDetailedErrorResponse extends MastodonErrorResponse{
	public Map<String, List<FieldError>> detailedErrors;

	public MastodonDetailedErrorResponse(String error, int httpStatus, Throwable exception){
		super(error, httpStatus, exception);
	}

	public static class FieldError{
		public String error;
		public String description;
	}
}
