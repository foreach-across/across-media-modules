package com.foreach.imageserver.core.services.support;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.services.ImageModificationResolverImpl;
import com.foreach.imageserver.dto.ImageResolutionDto;

import java.util.Map;
import java.util.Optional;

/**
 * Selector api to find a single modification that fits a certain resolution and context request.
 * Mainly for internal use, see the {@link ImageModificationResolverImpl}.
 *
 * @author Arne Vandamme
 */
public interface ImageModificationSelector
{
	/**
	 * Selects an appropriate modification for the resolution requested.
	 * The modification will be selected from the candidate map.  The candidate map contains the modification as
	 * key and the normalized output resolution for which the modification is registered as value.
	 *
	 * @param image                  source image
	 * @param candidateModifications candidates: modification + corresponding resolution
	 * @return modification if one found
	 */
	Optional<ImageModification> selectImageModification( Image image,
	                                                     Map<ImageModification, ImageResolutionDto> candidateModifications,
	                                                     ImageResolutionDto imageResolutionRequested );
}
