package com.foreach.imageserver.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an image transform command.
 * Contains a chain of {@link ImageTransformDto} to be applied to a single source image,
 * optionally to one or more pages of the source image.
 * <p>
 * todo: wip
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Data
@Builder(toBuilder = true)
public class ImageTransformCommandDto
{
	/**
	 * Optional identifier pattern for the result. In case of multiple pages this
	 * may contain a wildcard marker (<strong>*</strong>) where the scene number will be inserted.
	 * If no result id specified, a generic id will be assigned.
	 */
	private String resultId;

	/**
	 *
	 */
	private String pages;

	private List<ImageTransformDto> transforms = new ArrayList<>();
}
