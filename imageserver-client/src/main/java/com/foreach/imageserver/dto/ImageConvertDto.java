package com.foreach.imageserver.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DTO used when converting an image into 1 or multiple transformations
 *
 * @author Wouter Van Hecke
 * @since 5.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertDto
{
	/**
	 * The image that we want to transform.
	 * Either this property or {@link #imageId} should be set (to convert a registered image).
	 */
	private byte[] image;

	/**
	 * Id of the registered image that we want to transform.
	 * Either this property or {@link #image} bytes should be set.
	 */
	private String imageId;

	/**
	 * Which page number / scene numbers do we want to transform?
	 * <p>
	 * Value can be a number (1), a range (3-6) and multiple numbers can be seperated using a comma.
	 * For example: value "1,4-6" will return page 1, 4 and 5.
	 * This index is 0-based (so number 0 will return page 1)
	 */
	private String pages;

	/**
	 * A collection of transformations we want to perform on the image.
	 * <p>
	 * Every entry consists of a key and a list of transformations.
	 * <p>
	 * The key can be used to identify the correct image in the response. In the key, you can use the wildcard *. This value will be replaced with the page
	 * number in the returning key. So if you supply "flower-*" as key and you convert page 1 and 2, the response will contain an image with key "flower-1" and
	 * "flower-2".
	 * <p>
	 * The list of transformations contains of all the transformations you want to execute on the image. These will be executed in the supplied order.
	 */
	@Singular
	private Map<String, List<ImageTransformDto>> transformations;
}
