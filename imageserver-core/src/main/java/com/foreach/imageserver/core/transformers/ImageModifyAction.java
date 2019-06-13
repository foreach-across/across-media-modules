package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;

public class ImageModifyAction
{
	private final ImageSource sourceImageSource;
	private final Dimensions outputDimensions;
	private final Crop crop;
	private final Dimensions density;
	private final Dimensions boundaries;
	private final ImageType outputType;

	public ImageModifyAction( SimpleImageSource sourceImageSource,
	                          int outputWidth,
	                          int outputHeight,
	                          int cropX,
	                          int cropY,
	                          int cropWidth,
	                          int cropHeight,
	                          int densityWidth,
	                          int densityHeight,
	                          ImageType outputType ) {
		this( sourceImageSource, outputWidth, outputHeight, cropX, cropY, cropWidth, cropHeight, densityWidth, densityHeight, outputType, null );
	}

	public ImageModifyAction( ImageSource sourceImageSource,
	                          int outputWidth,
	                          int outputHeight,
	                          int cropX,
	                          int cropY,
	                          int cropWidth,
	                          int cropHeight,
	                          int densityWidth,
	                          int densityHeight,
	                          ImageType outputType,
	                          Dimensions boundaries ) {
		this.sourceImageSource = sourceImageSource;
		this.outputDimensions = new Dimensions( outputWidth, outputHeight );
		this.crop = new Crop( cropX, cropY, cropWidth, cropHeight );
		this.density = new Dimensions( densityWidth, densityHeight );
		this.outputType = outputType;
		this.boundaries = boundaries;
	}

	public ImageSource getSourceImageSource() {
		return sourceImageSource;
	}

	public Dimensions getOutputDimensions() {
		return outputDimensions;
	}

	public Crop getCrop() {
		return crop;
	}

	public Dimensions getDensity() {
		return density;
	}

	public ImageType getOutputType() {
		return outputType;
	}

	public Dimensions getBoundaries() {
		return boundaries;
	}
}
