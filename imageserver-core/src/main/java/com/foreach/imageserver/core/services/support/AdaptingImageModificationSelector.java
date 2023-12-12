package com.foreach.imageserver.core.services.support;

import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.math.AspectRatio;
import com.foreach.imageserver.math.ImageServerConversionUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.foreach.imageserver.core.services.support.AspectRatioImageModificationSelector.compareDistanceToRequestedResolution;
import static com.foreach.imageserver.math.ImageServerConversionUtils.calculateAspectRatio;
import static java.util.stream.Collectors.toMap;

/**
 * Finds the best modification that could be extended or cropped to fit the resolution requested.
 * Will only take modifications into account for resolutions that do not have the same aspect
 * ratio as the original resolution, as those would not need adapting anyway.
 * <p/>
 * Uses the output resolution of the modification to determine how it best fits the requested resolution.
 * <p/>
 * Uses the following rules:
 * <ul>
 * <li>remove all modifications for the same aspect ratio</li>
 * <li>order by absolute distance to aspect ratio then absolute resolution distance</li>
 * <li>try extending existing crops from the center out so they fit</li>
 * <li>if none found, cut existing crop toward the center point and see if enough datapoints remain</li>
 * </ul>
 *
 * @author Arne Vandamme
 * @see AspectRatioImageModificationSelector
 */
@OrderInModule(3)
public class AdaptingImageModificationSelector implements ImageModificationSelector
{
	@Override
	public Optional<ImageModification> selectImageModification( Image image,
	                                                            Map<ImageModification, ImageResolutionDto> candidateModifications,
	                                                            ImageResolutionDto imageResolutionRequested ) {
		Map<ImageModification, ImageResolutionDto> actualCandidates
				= orderCropCandidates( candidateModifications, imageResolutionRequested );

		ImageModification modification = extendExistingCrop( image, actualCandidates, imageResolutionRequested );

		if ( modification == null ) {
			modification = shrinkExistingCrop( actualCandidates, imageResolutionRequested );
		}

		return Optional.ofNullable( modification );
	}

	private ImageModification shrinkExistingCrop(
			Map<ImageModification, ImageResolutionDto> candidates,
			ImageResolutionDto imageResolutionRequested ) {
		AspectRatio requestedRatio = calculateAspectRatio( imageResolutionRequested );
		DimensionsDto requestedDimensions = imageResolutionRequested.getDimensions();

		for ( Map.Entry<ImageModification, ImageResolutionDto> candidate : candidates.entrySet() ) {
			ImageModification existingModification = candidate.getKey();
			CropDto existingCrop = DtoUtil.toDto( existingModification.getCrop() );
			CropDto shrankCrop = ImageServerConversionUtils.shrinkCrop( existingCrop, requestedRatio );

			if ( ImageServerConversionUtils.fitsIn( requestedDimensions, shrankCrop.getDimensions() ) ) {
				ImageModification customModification = clone( existingModification );
				customModification.setCrop( DtoUtil.toBusiness( shrankCrop ) );

				return customModification;
			}
		}

		return null;
	}

	private ImageModification extendExistingCrop(
			Image image,
			Map<ImageModification, ImageResolutionDto> candidates,
			ImageResolutionDto imageResolutionRequested ) {
		AspectRatio requestedRatio = calculateAspectRatio( imageResolutionRequested );
		DimensionsDto imageDimensions = DtoUtil.toDto( image.getDimensions() );

		for ( Map.Entry<ImageModification, ImageResolutionDto> candidate : candidates.entrySet() ) {
			ImageModification existingModification = candidate.getKey();
			CropDto existingCrop = DtoUtil.toDto( existingModification.getCrop() );
			CropDto extendedCrop = ImageServerConversionUtils.extendCrop( existingCrop, requestedRatio );

			if ( ImageServerConversionUtils.isWithinBox( extendedCrop, imageDimensions ) ) {
				ImageModification customModification = clone( existingModification );
				customModification.setCrop( DtoUtil.toBusiness( extendedCrop ) );

				return customModification;
			}
		}

		return null;
	}

	private ImageModification clone( ImageModification existingModification ) {
		ImageModification customModification = new ImageModification();
		customModification.setResolutionId( existingModification.getResolutionId() );
		customModification.setContextId( existingModification.getContextId() );
		customModification.setImageId( existingModification.getImageId() );
		customModification.setDensity( existingModification.getDensity() );
		return customModification;
	}

	/**
	 * Remove resolutions for the same aspect ratio - these should have been handled previously.
	 * Sort by absolute distance to ratio, then absolute resolution distance.
	 */
	private Map<ImageModification, ImageResolutionDto> orderCropCandidates(
			Map<ImageModification, ImageResolutionDto> candidateModifications,
			ImageResolutionDto imageResolutionRequested ) {
		AspectRatio requestedRatio = calculateAspectRatio( imageResolutionRequested );

		return candidateModifications
				.entrySet()
				.stream()
				.filter( e -> !requestedRatio.equals( calculateAspectRatio( e.getValue() ) )
						&& !calculateAspectRatio( e.getValue() ).isUndefined() )
				.sorted( ( left, right ) -> {
					double arDistanceLeft = distanceToAspectRatio( requestedRatio, left.getValue() );
					double arDistanceRight = distanceToAspectRatio( requestedRatio, right.getValue() );

					int compareValue = Double.compare( arDistanceLeft, arDistanceRight );

					if ( compareValue == 0 ) {
						return compareDistanceToRequestedResolution(
								imageResolutionRequested, left.getValue(), right.getValue()
						);
					}

					return compareValue;

				} )
				.collect( toMap( Map.Entry::getKey, Map.Entry::getValue, ( l, r ) -> l, LinkedHashMap::new ) );
	}

	private double distanceToAspectRatio( AspectRatio requestedRatio, ImageResolutionDto resolution ) {
		AspectRatio other = calculateAspectRatio( resolution );

		return Math.abs( requestedRatio.getNumerator() * 1.0d / requestedRatio.getDenominator() )
				- ( other.getNumerator() * 1.0d / other.getDenominator() );
	}
}
