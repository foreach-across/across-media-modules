package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Arne Vandamme
 */
public class ListResolutionsResponse extends ListResolutionsRequest
{
	private boolean contextDoesNotExist;
	private List<ImageResolutionDto> imageResolutions = Collections.emptyList();

	public ListResolutionsResponse() {
	}

	public ListResolutionsResponse( ListResolutionsRequest request ) {
		BeanUtils.copyProperties( request, this );
	}

	public boolean isContextDoesNotExist() {
		return contextDoesNotExist;
	}

	public void setContextDoesNotExist( boolean contextDoesNotExist ) {
		this.contextDoesNotExist = contextDoesNotExist;
	}

	public List<ImageResolutionDto> getImageResolutions() {
		return imageResolutions;
	}

	public void setImageResolutions( List<ImageResolutionDto> imageResolutions ) {
		this.imageResolutions = imageResolutions;
	}
}
