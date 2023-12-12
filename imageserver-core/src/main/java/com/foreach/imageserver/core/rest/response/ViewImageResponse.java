package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.transformers.ImageSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * @author Arne Vandamme
 */
public class ViewImageResponse extends ImageResponse
{
	private boolean resolutionDoesNotExist, failed;

	@Getter
	@Setter
	private ImageSource imageSource;

	public ViewImageResponse() {
	}

	public ViewImageResponse( ViewImageRequest request ) {
		BeanUtils.copyProperties( request, this );
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
}
