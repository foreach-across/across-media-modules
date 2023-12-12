package com.foreach.imageserver.core.rest.request;

/**
 * @author Arne Vandamme
 */
public class ImageRequest
{
	private String externalId;
	private String context;

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId( String externalId ) {
		this.externalId = externalId;
	}

	public String getContext() {
		return context;
	}

	public void setContext( String context ) {
		this.context = context;
	}
}
