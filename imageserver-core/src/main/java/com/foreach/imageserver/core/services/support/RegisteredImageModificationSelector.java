package com.foreach.imageserver.core.services.support;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.Optional;

/**
 * Will only return the modification registered to the requested resolution.
 * A modification linked to a resolution with the same id will be returned immediately, else
 * the first modification that is linked to a resolution with the same dimensions will be returned.
 * <p/>
 * Note that it is possible for the candidates to contain more than one resolution with the same dimension,
 * as long as the context of the modification is different.
 *
 * @author Arne Vandamme
 * @since 4.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RegisteredImageModificationSelector implements ImageModificationSelector
{
	@Override
	public Optional<ImageModification> selectImageModification( Image image,
	                                                            Map<ImageModification, ImageResolutionDto> candidateModifications,
	                                                            ImageResolutionDto imageResolutionRequested ) {

		ImageModification modificationForResolution = null;

		for ( Map.Entry<ImageModification, ImageResolutionDto> candidate : candidateModifications.entrySet() ) {
			if ( imageResolutionRequested.getId() == candidate.getValue().getId() ) {
				// return a modification registered to the resolution with the same id immediately
				return Optional.ofNullable( candidate.getKey() );
			}
			else if ( imageResolutionRequested.equals( candidate.getValue() ) && modificationForResolution == null ) {
				// select the first modification for the resolution with the same dimensions
				modificationForResolution = candidate.getKey();
			}
		}

		return Optional.ofNullable( modificationForResolution );
	}
}
