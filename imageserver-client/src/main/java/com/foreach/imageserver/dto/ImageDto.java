package com.foreach.imageserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Object that respresents an image
 *
 * @author Wouter Van Hecke
 * @since 5.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDto
{
	/**
	 * The image in bytes
	 */
	private byte[] image;

	/**
	 * The format of the images (example, PNG, JPEG, PDF, ...)
	 */
	private ImageTypeDto format;
}
