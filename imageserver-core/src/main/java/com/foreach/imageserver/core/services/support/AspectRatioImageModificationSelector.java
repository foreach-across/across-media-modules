package com.foreach.imageserver.core.services.support;

import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.math.AspectRatio;
import com.foreach.imageserver.math.ImageServerConversionUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.foreach.imageserver.math.ImageServerConversionUtils.calculateAspectRatio;
import static com.foreach.imageserver.math.ImageServerConversionUtils.calculateDistance;
import static java.util.stream.Collectors.toMap;

/**
 * Finds the most appropriate modification with the same aspect ratio as the resolution requested.
 * From all candidates for a resolution with the same aspect ratio:
 * <ul>
 * <li>will take the closest possible higher resolution</li>
 * <li>if none found, will take the closest possible lower resolution where the crop has enough datapoints</li>
 * </ul>
 *
 * @author Arne Vandamme
 * @since 4.0.0
 */
@OrderInModule(2)
public class AspectRatioImageModificationSelector implements ImageModificationSelector
{
	@Override
	public Optional<ImageModification> selectImageModification( Image image,
	                                                            Map<ImageModification, ImageResolutionDto> candidateModifications,
	                                                            ImageResolutionDto imageResolutionRequested ) {
		Map<ImageModification, ImageResolutionDto> actualCandidates
				= filterByAspectRatioAndOrderByDistance( candidateModifications, imageResolutionRequested );

		for ( Map.Entry<ImageModification, ImageResolutionDto> candidate : actualCandidates.entrySet() ) {
			int distance = calculateDistance( imageResolutionRequested, candidate.getValue() );

			// only return smaller if the requested resolution fits in the defined crop
			if ( distance >= 0 || fitsInModificationCrop( imageResolutionRequested, candidate.getKey() ) ) {
				return Optional.of( candidate.getKey() );
			}
		}

		return Optional.empty();
	}

	private boolean fitsInModificationCrop( ImageResolutionDto imageResolutionRequested,
	                                        ImageModification modification ) {
		return ImageServerConversionUtils.fitsIn(
				imageResolutionRequested.getDimensions(), DtoUtil.toDto( modification.getCrop().getDimensions() )
		);
	}

	/**
	 * Only keep resolutions with the same aspect ratio.  Order them:
	 * <ul>
	 * <li>first larger resolutions starting with the smallest</li>
	 * <li>then smaller resolutions starting with the largest</li>
	 * </ul>
	 */
	private Map<ImageModification, ImageResolutionDto> filterByAspectRatioAndOrderByDistance(
			Map<ImageModification, ImageResolutionDto> candidateModifications,
			ImageResolutionDto imageResolutionRequested ) {
		AspectRatio requestedRatio = calculateAspectRatio( imageResolutionRequested );

		return candidateModifications
				.entrySet()
				.stream()
				.filter( e -> requestedRatio.equals( calculateAspectRatio( e.getValue() ) ) )
				.sorted( ( left, right ) -> compareDistanceToRequestedResolution( imageResolutionRequested,
				                                                                  left.getValue(), right.getValue() ) )
				.collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( l, r ) -> l, LinkedHashMap::new ) );
	}

	static int compareDistanceToRequestedResolution( ImageResolutionDto requested,
	                                                 ImageResolutionDto left,
	                                                 ImageResolutionDto right ) {
		int distanceToLeft = calculateDistance( requested, left );
		int distanceToRight = calculateDistance( requested, right );

		if ( distanceToLeft < 0 && distanceToRight > 0 ) {
			return 1;
		}
		else if ( distanceToLeft > 0 && distanceToRight < 0 ) {
			return -1;
		}
		else if ( distanceToLeft > 0 ) {
			return Integer.compare( distanceToLeft, distanceToRight );
		}

		return -Integer.compare( distanceToLeft, distanceToRight );
	}
}
