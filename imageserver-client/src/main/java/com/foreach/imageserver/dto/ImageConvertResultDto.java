package com.foreach.imageserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * Result object of an image conversion call
 *
 * @author Wouter Van Hecke
 * @since 5.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertResultDto
{
	/**
	 * The total number of transforms in this object
	 */
	private int total;

	/**
	 * A collection that contains all the pagenumbers / scenenumbers that were rendered
	 */
	private Collection<Integer> pages;

	/**
	 * A collection with all the keys in this object
	 */
	private Collection<String> keys;

	/**
	 * A collection of converted images
	 */
	private Map<String, ImageDto> transforms;
}
