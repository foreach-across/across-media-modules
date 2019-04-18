package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.ImageSource;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageTransformDto;

import java.io.InputStream;
import java.util.Collection;

public interface ImageTransformService
{
	Dimensions computeDimensions( StreamImageSource imageSource );

	ImageAttributes getAttributes( InputStream imageStream );

	InMemoryImageSource modify( StreamImageSource imageSource,
	                            int outputWidth,
	                            int outputHeight,
	                            int cropX,
	                            int cropY,
	                            int cropWidth,
	                            int cropHeight,
	                            int densityWidth,
	                            int densityHeight,
	                            ImageType outputType );

	InMemoryImageSource modify( StreamImageSource imageSource,
	                            int outputWidth,
	                            int outputHeight,
	                            int cropX,
	                            int cropY,
	                            int cropWidth,
	                            int cropHeight,
	                            int densityWidth,
	                            int densityHeight,
	                            ImageType outputType,
	                            Dimensions boundaries );

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
