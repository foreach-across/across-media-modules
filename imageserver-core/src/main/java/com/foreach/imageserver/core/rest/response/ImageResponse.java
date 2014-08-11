package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.core.rest.request.ImageRequest;

/**
 * @author Arne Vandamme
 */
public class ImageResponse extends ImageRequest
{
	private boolean imageDoesNotExist, contextDoesNotExist;
	private boolean outputTypeNotAllowed;

	public boolean isImageDoesNotExist() {
		return imageDoesNotExist;
	}

	public void setImageDoesNotExist( boolean imageDoesNotExist ) {
		this.imageDoesNotExist = imageDoesNotExist;
	}

	public boolean isContextDoesNotExist() {
		return contextDoesNotExist;
	}

	public void setContextDoesNotExist( boolean contextDoesNotExist ) {
		this.contextDoesNotExist = contextDoesNotExist;
	}

	public void setOutputTypeNotAllowed( boolean outputTypeNotAllowed ) {
		this.outputTypeNotAllowed = outputTypeNotAllowed;
	}

	public boolean isOutputTypeNotAllowed() {
		return outputTypeNotAllowed;
	}
}
