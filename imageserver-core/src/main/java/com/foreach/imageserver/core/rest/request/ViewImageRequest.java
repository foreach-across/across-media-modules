package com.foreach.imageserver.core.rest.request;

import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;

/**
 * @author Arne Vandamme
 */
public class ViewImageRequest
{
	private String externalId;
	private String context;
	private ImageResolutionDto imageResolutionDto;
	private ImageModificationDto imageModificationDto;
	private ImageVariantDto imageVariantDto;

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId( String externalId ) {
		this.externalId = externalId;
	}

	public String getContext() {
		return context;
	}

	public void setContext( String context ) {
		this.context = context;
	}

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
