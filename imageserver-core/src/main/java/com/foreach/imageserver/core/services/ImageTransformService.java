package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public interface ImageTransformService
{
	Logger LOG = LoggerFactory.getLogger( ImageTransformService.class );

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
	                            Dimensions boundingBox );
}
