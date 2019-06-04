package com.foreach.imageserver.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * DTO used when converting an existing image, as referenced by id, into 1 or multiple transformations
 *
 * @author Benny Lootens
 * @since 5.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImageByIdConvertDto extends ImageConvertDto
{
	/**
	 * The image that we want to transform
	 */
	private String imageId;

	/**
	 * The context in which the transformations must take place
	 */
	private String context;

	public ImageByIdConvertDto() {
		super( null, null, null );
	}

	public ImageByIdConvertDto( String pages, Map<String, List<ImageTransformDto>> transformations, String imageId, String context ) {
		super( null, pages, transformations );
		this.imageId = imageId;
		this.context = context;
	}
}
