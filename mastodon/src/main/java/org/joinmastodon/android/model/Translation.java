package org.joinmastodon.android.model;

import org.joinmastodon.android.api.AllFieldsAreRequired;

@AllFieldsAreRequired
public class Translation extends BaseModel{
	public String content;
	public String detectedSourceLanguage;
	public String provider;
	public MediaAttachment[] mediaAttachments;
	public PollTranslation poll;

	public static class MediaAttachment {
		public String id;
		public String description;
	}

	public static class PollTranslation {
		public String id;
		public PollOption[] options;
	}

	public static class PollOption {
		public String title;
	}
}
