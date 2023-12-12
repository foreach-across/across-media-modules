package com.foreach.imageserver.core.rest.request;

import com.foreach.imageserver.dto.ImageModificationDto;
import java.util.List;

/**
 * @author Arne Vandamme
 */
public class RegisterModificationRequest extends ImageRequest
{
	private ImageModificationDto imageModificationDto;

	private List<ImageModificationDto> imageModificationDtos;


	public ImageModificationDto getImageModificationDto() {
		return imageModificationDto;
	}

	public void setImageModificationDto( ImageModificationDto imageModificationDto ) {
		this.imageModificationDto = imageModificationDto;
	}

	public void setImageModificationDtos( List<ImageModificationDto> imageModificationDtos ) {
		this.imageModificationDtos = imageModificationDtos;
	}

	public List<ImageModificationDto> getImageModificationDtos() {
		return imageModificationDtos;
	}
}
