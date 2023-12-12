package com.foreach.imageserver.core.rest.response;

import com.foreach.imageserver.dto.ImageResolutionDto;

import java.util.Collections;
import java.util.List;

/**
 * @author Arne Vandamme
 */
public class PregenerateResolutionsResponse
{
	private boolean imageDoesNotExist;
	private List<ImageResolutionDto> imageResolutions = Collections.emptyList();

	public boolean isImageDoesNotExist() {
		return imageDoesNotExist;
	}

	public void setImageDoesNotExist( boolean imageDoesNotExist ) {
		this.imageDoesNotExist = imageDoesNotExist;
	}

	public List<ImageResolutionDto> getImageResolutions() {
		return imageResolutions;
	}

	public void setImageResolutions( List<ImageResolutionDto> imageResolutions ) {
		this.imageResolutions = imageResolutions;
	}
}
