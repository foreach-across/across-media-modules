package com.foreach.imageserver.core.business;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The ImageVariant specifies what variant of an ImageModification we want to obtain. This may include options such as
 * the desired output format, whether the image should be converted to greyscale, etc.
 * <p/>
 * Note that this is very different from the ImageModification itself, which specifies generically how we should go from an
 * original image to an image having a certain resolution. For each ImageModification, every combination of ImageVariant
 * options can be requested.
 * <p/>
 * Note that we do not persist in the database which variants we have already created. Maintaining no database state
 * of what exists on the filesystem makes for convenient housekeeping. Every image on the filesystem can be removed
 * at will; it will be refetched and/or recreated when necessary.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public class ImageVariant
{
	private ImageType outputType;

	public ImageType getOutputType() {
		return outputType;
	}

	public void setOutputType( ImageType outputType ) {
		this.outputType = outputType;
	}
}
