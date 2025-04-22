package net.seqular.network.model;

import net.seqular.network.api.RequiredField;

/**
 * Represents an OAuth token used for authenticating with the API and performing actions.
 */
public class Token extends BaseModel{
	/**
	 * An OAuth token to be used for authorization.
	 */
	@RequiredField
	public String accessToken;
	/**
	 * The OAuth token type. Mastodon uses Bearer tokens.
	 */
	public String tokenType;
	/**
	 * The OAuth scopes granted by this token, space-separated.
	 */
	public String scope;
	/**
	 * When the token was generated.
	 * (unixtime)
	 */
	@RequiredField
	public long createdAt;
}
