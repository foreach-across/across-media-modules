package com.foreach.imageserver.core.rest.request;

import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;

/**
 * @author Arne Vandamme
 */
public class ViewImageRequest extends ImageRequest
{
	private ImageResolutionDto imageResolutionDto;
	private ImageModificationDto imageModificationDto;
	private ImageVariantDto imageVariantDto;

	public ImageResolutionDto getImageResolutionDto() {
		return imageResolutionDto;
	}

	public void setImageResolutionDto( ImageResolutionDto imageResolutionDto ) {
		this.imageResolutionDto = imageResolutionDto;
	}

	public ImageVariantDto getImageVariantDto() {
		return imageVariantDto;
	}

	public void setImageVariantDto( ImageVariantDto imageVariantDto ) {
		this.imageVariantDto = imageVariantDto;
	}

	public ImageModificationDto getImageModificationDto() {
		return imageModificationDto;
	}

	public void setImageModificationDto( ImageModificationDto imageModificationDto ) {
		this.imageModificationDto = imageModificationDto;
	}
}
