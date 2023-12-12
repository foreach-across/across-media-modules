package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.ImageType;

import java.io.InputStream;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public interface ImageSource
{
	/**
	 * @return type of the image
	 */
	ImageType getImageType();

	/**
	 * @return stream to the image byte data
	 */
	InputStream getImageStream();
}
