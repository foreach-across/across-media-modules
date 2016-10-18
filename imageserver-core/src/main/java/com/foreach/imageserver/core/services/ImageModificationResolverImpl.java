package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.support.ImageModificationSelector;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.math.AspectRatio;
import com.foreach.imageserver.math.ImageServerConversionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Default implementation of {@link ImageModificationResolver} that attempts to fetch the most
 * appropriate modification.  The following precedence rules are applied when returning the modification:
 * <ol>
 * <li>modification registered to the resolution and context requested</li>
 * <li>modification registered to a higher resolution with the same aspect ratio in the same context</li>
 * <li>modification registered to a lower resolution with the same aspect ratio in the same context,
 * if the surface of that modification is large enough to cover the requested output resolution
 * </li>
 * <li>modification registered to that resolution in any other context</li>
 * <li>modification registered to a higher resolution with the same aspect ratio in another context</li>
 * <li>modification registered to a lower resolution with the same aspect ration in another context,
 * if the surface of that modification is large enough to cover the requested output resolution</li>
 * <li>valid modification built by either extending or shrinking the modification of the resolution closest
 * to the output resolution (a modification is not valid if the crop would extend beyond image boundaries)</li>
 * <li>largest modification with that aspect ratio that can be generated from the center of the image</li>
 * </ol>
 * If a registered modification is being used, the resolution id for which that modification was registered
 * will be present on the {@link ImageModificationDto} returned.
 * <p/>
 * NOTE: All candidate modifications are based on the output resolution they are supposed to generate, not the
 * actual crop data.  A modification for a resolution with aspect ratio A, should also have an aspect ratio of A.
 * The actual dimensions of the crop are not considered (except for boundary checking).
 *
 * @author Arne Vandamme
 */
public class ImageModificationResolverImpl implements ImageModificationResolver
{
	private CropGeneratorUtil cropGeneratorUtil;

	private ImageService imageService;

	@RefreshableCollection
	private Collection<ImageModificationSelector> imageModificationSelectors = Collections.emptyList();

	@Autowired
	public void setCropGeneratorUtil( CropGeneratorUtil cropGeneratorUtil ) {
		Assert.notNull( cropGeneratorUtil );
		this.cropGeneratorUtil = cropGeneratorUtil;
	}

	@Autowired
	public void setImageService( ImageService imageService ) {
		this.imageService = imageService;
	}

	public void setImageModificationSelectors( List<ImageModificationSelector> imageModificationSelectors ) {
		Assert.notNull( imageModificationSelectors );
		this.imageModificationSelectors = imageModificationSelectors;
	}

	@Override
	public ImageModificationDto resolveModification( Image image,
	                                                 ImageContext context,
	                                                 ImageResolution imageResolution ) {
		Assert.notNull( image );
		Assert.notNull( context );
		Assert.notNull( imageResolution );

		ImageResolutionDto outputResolution = buildOutputResolution( image, imageResolution );

		ImageModificationDto modificationDto = new ImageModificationDto();
		modificationDto.setResolution( outputResolution );

		// find existing modification
		Optional<ImageModification> imageModification
				= selectBestFittingModification( image, context, modificationDto.getResolution() );

		imageModification.ifPresent( modification -> {
			modificationDto.setBaseResolutionId( modification.getResolutionId() );
			modificationDto.setCrop( DtoUtil.toDto( modification.getCrop() ) );
		} );

		cropGeneratorUtil.normalizeModificationDto( image, modificationDto );

		return modificationDto;
	}

	private ImageResolutionDto buildOutputResolution( Image image, ImageResolution resolutionRequested ) {
		Dimensions dimensions = cropGeneratorUtil.applyResolution( image, resolutionRequested );
		ImageResolutionDto resolutionDto = DtoUtil.toDto( resolutionRequested );
		resolutionDto.setId( resolutionRequested.getId() );
		resolutionDto.setWidth( dimensions.getWidth() );
		resolutionDto.setHeight( dimensions.getHeight() );

		return resolutionDto;
	}

	private Optional<ImageModification> selectBestFittingModification( Image image,
	                                                                   ImageContext context,
	                                                                   ImageResolutionDto resolutionDto ) {
		Map<ImageModification, ImageResolutionDto> modificationMap = buildExistingModificationsMap( image, context );

		for ( ImageModificationSelector selector : imageModificationSelectors ) {
			Optional<ImageModification> modification
					= selector.selectImageModification( image, modificationMap, resolutionDto );
			if ( modification.isPresent() ) {
				return modification;
			}
		}

		return Optional.empty();
	}

	private Map<ImageModification, ImageResolutionDto> buildExistingModificationsMap( Image image,
	                                                                                  ImageContext context ) {
		Map<Long, ImageResolution> resolutionsById = imageService
				.getAllResolutions()
				.stream()
				.collect( toMap( ImageResolution::getId, Function.identity() ) );

		return imageService.getAllModifications( image.getId() ).stream()
		                   .sorted( Comparator.comparingInt( m -> m.getContextId() == context.getId() ? 0 : 1 ) )
		                   .collect( toMap(
				                   Function.identity(),
				                   mod -> resolutionForImage( image, resolutionsById.get( mod.getResolutionId() ) ),
				                   ( l, r ) -> l,
				                   LinkedHashMap::new
		                   ) );
	}

	private ImageResolutionDto resolutionForImage( Image image, ImageResolution resolution ) {
		AspectRatio aspectRatio =
				new AspectRatio( image.getDimensions().getWidth(), image.getDimensions().getHeight() );

		DimensionsDto normalized
				= ImageServerConversionUtils.normalize( DtoUtil.toDto( resolution.getDimensions() ), aspectRatio );
		ImageResolutionDto resolutionDto = new ImageResolutionDto( normalized.getWidth(), normalized.getHeight() );
		resolutionDto.setId( resolution.getId() );

		return resolutionDto;
	}
}
