package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.dto.ImageTransformDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a transformation to be performed on a particular image.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Builder(toBuilder = true)
@Getter
public class ImageTransformCommand extends ImageCommand<ImageSource>
{
	/**
	 * Image to be transformed.
	 */
	@NonNull
	private final ImageSource originalImage;

	/**
	 * Attributes of the image to be transformed.
	 */
	@NonNull
	private final ImageAttributes originalImageAttributes;

	/**
	 * Transform to apply.
	 */
	@NonNull
	private final ImageTransformDto transform;
}
