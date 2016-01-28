package com.foreach.imageserver.core.rest.request;

import com.foreach.imageserver.dto.ImageAspectRatioDto;
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
	private ImageAspectRatioDto imageAspectRatioDto;
	private byte[] imageData;

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

	public ImageAspectRatioDto getImageAspectRatioDto() {
		return imageAspectRatioDto;
	}

	public void setImageAspectRatioDto( ImageAspectRatioDto imageAspectRatioDto ) {
		this.imageAspectRatioDto = imageAspectRatioDto;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData( byte[] imageData ) {
		this.imageData = imageData;
	}
}
