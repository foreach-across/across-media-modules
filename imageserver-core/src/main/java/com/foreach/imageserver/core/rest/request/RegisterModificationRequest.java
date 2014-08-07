package com.foreach.imageserver.core.rest.request;

import com.foreach.imageserver.dto.ImageModificationDto;

/**
 * @author Arne Vandamme
 */
public class RegisterModificationRequest extends ImageRequest
{
	private ImageModificationDto imageModificationDto;

	public ImageModificationDto getImageModificationDto() {
		return imageModificationDto;
	}

	public void setImageModificationDto( ImageModificationDto imageModificationDto ) {
		this.imageModificationDto = imageModificationDto;
	}
}
