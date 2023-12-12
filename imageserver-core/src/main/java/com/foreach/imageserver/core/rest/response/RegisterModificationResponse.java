package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.rest.request.RegisterModificationRequest;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Arne Vandamme
 */
public class RegisterModificationResponse extends ImageResponse
{
	private boolean cropOutsideOfImageBounds;
	private Set<ImageResolutionDto> missingResolutions = new HashSet<ImageResolutionDto>();

	public RegisterModificationResponse() {
	}

	public RegisterModificationResponse( RegisterModificationRequest request ) {
		BeanUtils.copyProperties( request, this );
	}

	public boolean isResolutionDoesNotExist() {
		return missingResolutions.size() > 0;
	}

	public void setCropOutsideOfImageBounds( boolean cropOutsideOfImageBounds ) {
		this.cropOutsideOfImageBounds = cropOutsideOfImageBounds;
	}

	public boolean isCropOutsideOfImageBounds() {
		return cropOutsideOfImageBounds;
	}

	public void addMissingResolution( ImageResolutionDto resolution ) {
		missingResolutions.add( resolution );
	}

	public Set<ImageResolutionDto> getMissingResolutions() {
		return missingResolutions;
	}
}
