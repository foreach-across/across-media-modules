package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.dto.ImageTransformDto;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * Replaces the {@link com.foreach.imageserver.core.services.CropGeneratorUtil}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Component
public class ImageTransformUtils
{
	/**
	 * Normalize a transform to be executable for an image with the given attributes.
	 * This will calculate all unknown dimensions and simplify components of the transform (eg perform crop coordinate normalization).
	 *
	 * @param transformDto transform
	 * @param attributes   of the image to which the transform should apply
	 * @return normalized transform
	 */
	public ImageTransformDto normalize( @NonNull ImageTransformDto transformDto, @NonNull ImageAttributes attributes ) {
		return null;
	}
}
