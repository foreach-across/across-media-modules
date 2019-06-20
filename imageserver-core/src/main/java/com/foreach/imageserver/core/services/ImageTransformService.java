package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.ImageSource;
import com.foreach.imageserver.dto.ImageTransformDto;

import java.io.InputStream;
import java.util.Collection;

public interface ImageTransformService
{
	/**
	 * Retrieve the attributes for an image.
	 *
	 * @param imageStream image data
	 * @return attributes
	 */
	ImageAttributes getAttributes( InputStream imageStream );

	/**
	 * Apply a set of transformations in order to a single image source.
	 * If there is more than one transform specified, the result of the previous will always service as input for the next.
	 *
	 * @param imageSource      to apply the transforms to
	 * @param sourceAttributes resolved attributes of the image source
	 * @param transforms       to apply in order
	 * @return transformed image
	 */
	ImageSource transform( ImageSource imageSource, ImageAttributes sourceAttributes, Collection<ImageTransformDto> transforms );
}
