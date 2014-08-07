package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import org.springframework.beans.BeanUtils;

/**
 * @author Arne Vandamme
 */
public class ViewImageResponse extends ViewImageRequest
{
	private boolean imageDoesNotExist, contextDoesNotExist, resolutionDoesNotExist, failed;

	private StreamImageSource imageSource;

	public ViewImageResponse() {
	}

	public ViewImageResponse( ViewImageRequest request ) {
		BeanUtils.copyProperties( request, this );
	}

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

	public boolean isResolutionDoesNotExist() {
		return resolutionDoesNotExist;
	}

	public void setResolutionDoesNotExist( boolean resolutionDoesNotExist ) {
		this.resolutionDoesNotExist = resolutionDoesNotExist;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed( boolean failed ) {
		this.failed = failed;
	}

	public StreamImageSource getImageSource() {
		return imageSource;
	}

	public void setImageSource( StreamImageSource imageSource ) {
		this.imageSource = imageSource;
	}
}
