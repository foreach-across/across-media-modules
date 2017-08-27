package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.ImageModificationDto;

/**
 * API responsible for determining the most appropriate modification to use when requesting
 * a particular output resolution of an image.
 *
 * @author Arne Vandamme
 * @see ImageModificationResolverImpl
 */
public interface ImageModificationResolver
{
	/**
	 * Resolve the most appropriate modification for the output variant requested.
	 *
	 * @param image           to generate a variant for
	 * @param context         the variant is being generated for
	 * @param imageResolution output resolution that is requested
	 * @return modification
	 */
	ImageModificationDto resolveModification( Image image, ImageContext context, ImageResolution imageResolution );
}
