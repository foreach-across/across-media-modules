package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import org.springframework.beans.BeanUtils;

/**
 * @author Arne Vandamme
 */
public class RegisterModificationResponse extends ImageResponse
{
	private boolean resolutionDoesNotExist;
	private boolean cropOutsideOfImageBounds;

	public RegisterModificationResponse() {
	}

	public RegisterModificationResponse( RegisterModificationRequest request ) {
		BeanUtils.copyProperties( request, this );
	}

	public boolean isResolutionDoesNotExist() {
		return resolutionDoesNotExist;
	}

	public void setResolutionDoesNotExist( boolean resolutionDoesNotExist ) {
		this.resolutionDoesNotExist = resolutionDoesNotExist;
	}

	public void setCropOutsideOfImageBounds( boolean cropOutsideOfImageBounds ) {
		this.cropOutsideOfImageBounds = cropOutsideOfImageBounds;
	}

	public boolean isCropOutsideOfImageBounds() {
		return cropOutsideOfImageBounds;
	}
}
