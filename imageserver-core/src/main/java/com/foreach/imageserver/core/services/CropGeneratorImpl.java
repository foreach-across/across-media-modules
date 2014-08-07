package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.managers.ImageProfileManager;
import com.foreach.imageserver.core.services.exceptions.ImageCouldNotBeRetrievedException;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.logging.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.foreach.imageserver.core.services.CropGeneratorUtil.*;

/**
 * This implementation will use the existing Crops for an Image so as to propose an as-good-as-possible Crop for any
 * ImageResolution. The algorithm will first turn every existing Crop into a CropCandidate that matches the requested
 * output resolution. Then, it will judge each crop based on a number of criteria and pick the best one.
 * <p/>
 * Some facts to keep in mind are:
 * -   We always consider the entire image to be an existing crop.
 * -   As a consequence, the algorithm will always produce an outcome.
 * -   Per business rules, we never scale up the source image; i.e. zooming in on an existing crop is not permitted.
 * -   As a consequence, Crops can never be bigger than the original image.
 * <p/>
 * To turn an existing Crop into a valid CropCandidate, two tactics are employed:
 * -   If the existing crop is smaller than the requested resolution, we extend the existing crop. That is to say: we
 * increase the width and height of the crop as necessary, keeping the existing crop as much as possible in the
 * center of the new crop. As the requested Crop is required to be smaller than the original Image, this will
 * always yield a result.
 * -   If the existing crop is larger than the requested resolution, we extend one dimension until the result has the
 * same aspect ratio as the requested resolution. Again, we keep the original crop at the center as much as
 * possible. Here, two things can happen: either the original Image is large enough to contain the extended Crop,
 * or it is not. In the former case, we can scale the resulting crop to the target resolution and the result will
 * still contain every bit of the original Image that was in the original Crop. In the latter case we'll have to
 * correct the Crop by cutting away portions of the original Image that were in the original Crop. This may of
 * course cause the Crop to be faulty (i.e. cutting away peoples heads, etc.).
 * <p/>
 * Then, we choose the most optimal CropCandidate:
 * -   First, we try to find a CropCandidate that does not require cutting, that is based on a Crop in the requested
 * Context and that requires the least amount of extending and scaling (i.e. so that the included bits of the
 * original Image will match the closest to an existing Crop).
 * -   If this does not yield a result, we do the same, this time for Crops in different Contexts.
 * -   As a last resort, we'll use any candidate that requires the least amount of cutting.
 * <p/>
 * TODO This implementation may not yield the desired results when used for vector images (as these can be upscaled).
 */
@Service
public class CropGeneratorImpl implements CropGenerator
{

	private static Logger LOG = LoggerFactory.getLogger( CropGeneratorImpl.class );

	@Autowired
	private ImageModificationManager imageModificationManager;

	@Autowired
	private ImageProfileManager imageProfileManager;

	@Override
	public ImageModificationDto buildModificationDto( Image image,
	                                                  ImageContext context,
	                                                  ImageResolution imageResolution ) {
		if ( image == null || context == null || imageResolution == null ) {
			LOG.warn(
					"Null parameters not allowed - CropGeneratorImpl#buildModificationDto: image={}, context={}, imageResolution={}",
					LogHelper.flatten( image, context, imageResolution ) );
		}

		ImageModification registeredModification =
				imageModificationManager.getById( image.getId(), context.getId(), imageResolution.getId() );

		ImageModificationDto modificationDto;

		if ( registeredModification != null ) {
			modificationDto = new ImageModificationDto( imageResolution.getWidth(), imageResolution.getHeight() );
			modificationDto.setCrop( DtoUtil.toDto( registeredModification.getCrop() ) );
		}
		else {
			ImageProfileModification profileModification =
					imageProfileManager.getModification( image.getImageProfileId(), context.getId(),
					                                     imageResolution.getId() );

			if ( profileModification != null ) {
				modificationDto = new ImageModificationDto( profileModification.getModificationDto() );
			}
			else {
				modificationDto = new ImageModificationDto( imageResolution.getWidth(), imageResolution.getHeight() );
				modificationDto.setCrop( DtoUtil.toDto( obtainCrop( image, context, imageResolution ) ) );
			}
		}

		CropGeneratorUtil.normalizeModificationDto( image, modificationDto );

		return modificationDto;
	}

	private Crop obtainCrop( Image image, ImageContext context, ImageResolution requestedResolution ) {
		if ( image == null || context == null || requestedResolution == null ) {
			LOG.warn(
					"Null parameters not allowed - CropGeneratorImpl#obtainCrop: image={}, context={}, requestedResolution={}",
					LogHelper.flatten( image, context, requestedResolution ) );
		}

		Crop result;

        /*
        ImageModification imageModification = imageModificationManager.getById(image.getId(), context.getId(), requestedResolution.getId());
        if (imageModification != null) {
            result = imageModification.getCrop();
        } else {
            // See if there is a default modification for the corresponding image profile
            // Todo: improve to keep more parameters of the original modification
            ImageProfileModification profileModification = imageProfileManager.getModification(image.getImageProfileId(), context.getId(), requestedResolution.getId());

            if (profileModification != null) {
                ImageModificationDto modificationDto = new ImageModificationDto();
                CropGeneratorUtil.normalizeModificationDto(image, modificationDto);

                CropDto cropDto = modificationDto.getCrop();
                result = new Crop(cropDto.getX(), cropDto.getY(), cropDto.getWidth(), cropDto.getHeight());
            } else {*/
		List<ImageModification> modifications = imageModificationManager.getAllModifications( image.getId() );
		result = generateCrop( image, context, requestedResolution, modifications );
		    /*}
	    }*/

		if ( result == null ) {
			throw new ImageCouldNotBeRetrievedException( "No crop could be determined for this image." );
		}

		return result;
	}

	@Override
	public Crop generateCrop( Image image,
	                          ImageContext context,
	                          ImageResolution resolution,
	                          List<ImageModification> modifications ) {
		if ( image == null || context == null || modifications == null ) {
			LOG.warn(
					"Null parameters not allowed - CropGeneratorImpl#generateCrop: image={}, context={}, modifications={}",
					LogHelper.flatten( image, context, modifications ) );
		}

		Dimensions requestedDimensions = resolution.getDimensions();
		Dimensions targetDimensions = applyResolution( image, resolution );
        /*
        if (targetDimensions.getWidth() > image.getDimensions().getWidth()) {
            return null;
        }
        if (targetDimensions.getHeight() > image.getDimensions().getHeight()) {
            return null;
        }*/

		// If requested resolution is larger than original image, return the largest possible image according to aspect
		targetDimensions = targetDimensions.scaleToFitIn( image.getDimensions() );

		// If the requested dimensions does not defined their own aspect ratio, we consider it a scale instead of an actual crop
		if ( requestedDimensions.fetchAspectRatio().isUndefined() ) {
			return new Crop( 0, 0, image.getDimensions().getWidth(), image.getDimensions().getHeight() );
		}

		Crops crops = obtainCrops( image, context, modifications );

		Set<CropCandidate> sameContextCandidates =
				calculateCropCandidates( image, targetDimensions, crops.getSameContext() );
		Set<CropCandidate> differentContextCandidates =
				calculateCropCandidates( image, targetDimensions, crops.getDifferentContext() );

		CropCandidate chosenCandidate = findBestNonCuttingCrop( sameContextCandidates );
		if ( chosenCandidate == null ) {
			chosenCandidate = findBestNonCuttingCrop( differentContextCandidates );
		}
		if ( chosenCandidate == null ) {
			Set<CropCandidate> allCandidates =
					new HashSet<>( sameContextCandidates.size() + differentContextCandidates.size() );
			allCandidates.addAll( sameContextCandidates );
			allCandidates.addAll( differentContextCandidates );
			chosenCandidate = findBestCuttingCrop( allCandidates );
		}

		return chosenCandidate.getCrop();
	}

	private CropCandidate findBestNonCuttingCrop( Set<CropCandidate> candidates ) {
		Iterator<CropCandidate> candidateIterator = candidates.iterator();

		CropCandidate firstNonCuttingCrop = findFirstNonCuttingCrop( candidateIterator );
		if ( firstNonCuttingCrop == null ) {
			return null;
		}

		CropCandidate smallestFitSoFar = firstNonCuttingCrop;
		CropCandidate bestFitSoFar = firstNonCuttingCrop;

		while ( candidateIterator.hasNext() ) {
			CropCandidate currentCandidate = candidateIterator.next();
			if ( !negligibleCutOffMeasure( currentCandidate ) ) {
				continue;
			}
			if ( currentCandidate.getExtensionMeasure() < smallestFitSoFar.getExtensionMeasure() ) {
				smallestFitSoFar = currentCandidate;
				if ( !withinAcceptedRange( bestFitSoFar,
				                           currentCandidate ) || ( currentCandidate.getScaleFactor() < bestFitSoFar.getScaleFactor() ) ) {
					bestFitSoFar = currentCandidate;
				}
			}
			else if ( withinAcceptedRange( currentCandidate,
			                               smallestFitSoFar ) && ( currentCandidate.getScaleFactor() < bestFitSoFar.getScaleFactor() ) ) {
				bestFitSoFar = currentCandidate;
			}
		}

		return bestFitSoFar;
	}

	private CropCandidate findBestCuttingCrop( Set<CropCandidate> candidates ) {
		CropCandidate chosenCandidate = null;
		for ( CropCandidate candidate : candidates ) {
			if ( negligibleCutOffMeasure( candidate ) ) {
				continue;
			}
			if ( ( chosenCandidate == null ) || ( candidate.getCutOffMeasure() < chosenCandidate.getCutOffMeasure() ) ) {
				chosenCandidate = candidate;
			}
		}
		return chosenCandidate;
	}

	private CropCandidate findFirstNonCuttingCrop( Iterator<CropCandidate> candidateIterator ) {
		while ( candidateIterator.hasNext() ) {
			CropCandidate nextCandidate = candidateIterator.next();
			if ( negligibleCutOffMeasure( nextCandidate ) ) {
				return nextCandidate;
			}
		}
		return null;
	}

	private boolean negligibleCutOffMeasure( CropCandidate candidate ) {
		return ( candidate.getCutOffMeasure() < 0.00001f );
	}

	private boolean withinAcceptedRange( CropCandidate candidateOne, CropCandidate candidateTwo ) {
		return ( Math.abs( candidateOne.getExtensionMeasure() - candidateTwo.getExtensionMeasure() ) <= 0.05 );
	}

	private Set<CropCandidate> calculateCropCandidates( Image image, Dimensions targetDimensions, Set<Crop> crops ) {
		Set<CropCandidate> cropCandidates = new HashSet<>( crops.size() );
		for ( Crop crop : crops ) {
			cropCandidates.add( calculateCropCandidate( image, targetDimensions, crop ) );
		}
		return cropCandidates;
	}

	private CropCandidate calculateCropCandidate( Image image, Dimensions targetDimensions, Crop crop ) {
		CropCandidate cropCandidate = calculateCropCandidateByExtending( image, targetDimensions, crop );
		if ( cropCandidate == null ) {
			cropCandidate = calculateCropCandidateByScaling( image, targetDimensions, crop );
		}
		return cropCandidate;
	}

	private CropCandidate calculateCropCandidateByExtending( Image image, Dimensions targetDimensions, Crop crop ) {
		if ( image == null || targetDimensions == null || crop == null ) {
			LOG.warn(
					"Null parameters not allowed - CropGeneratorImpl#calculateCropCandidateByExtending: image={}, targetDimensions={}, crop={}",
					LogHelper.flatten( image, targetDimensions, crop ) );
		}

		if ( crop.getWidth() > targetDimensions.getWidth() || crop.getHeight() > targetDimensions.getHeight() ) {
			return null;
		}

		int newWidth = targetDimensions.getWidth();
		int newHeight = targetDimensions.getHeight();

		int newX = crop.getX() + ( crop.getWidth() - newWidth ) / 2;
		if ( newX < 0 ) {
			newX = 0;
		}
		else if ( newX + newWidth > image.getDimensions().getWidth() ) {
			newX = image.getDimensions().getWidth() - newWidth;
		}

		int newY = crop.getY() + ( crop.getHeight() - newHeight ) / 2;
		if ( newY < 0 ) {
			newY = 0;
		}
		else if ( newY + newHeight > image.getDimensions().getHeight() ) {
			newY = image.getDimensions().getHeight() - newHeight;
		}

		Crop newCrop = new Crop( newX, newY, newWidth, newHeight );

		float newCropArea = area( newCrop );
		float existingCropArea = area( crop );

		float extensionMeasure = ( newCropArea - existingCropArea ) / newCropArea;
		float cutOffMeasure = 0.0f;
		float scaleFactor = 0.0f;

		return new CropCandidate( newCrop, extensionMeasure, cutOffMeasure, scaleFactor );
	}

	private CropCandidate calculateCropCandidateByScaling( Image image, Dimensions targetDimensions, Crop crop ) {
		if ( image == null || targetDimensions == null || crop == null ) {
			LOG.warn(
					"Null parameters not allowed - CropGeneratorImpl#calculateCropCandidateByScaling: image={}, targetDimensions={}, crop={}",
					LogHelper.flatten( image, targetDimensions, crop ) );
		}

		if ( targetDimensions.getWidth() > crop.getWidth() && targetDimensions.getHeight() > crop.getHeight() ) {
			return null;
		}

		int imageWidth = image.getDimensions().getWidth();
		int imageHeight = image.getDimensions().getHeight();

		float targetRatio = (float) targetDimensions.getWidth() / (float) targetDimensions.getHeight();
		float existingCropRatio = (float) crop.getWidth() / (float) crop.getHeight();

		int newX, newY, newWidth, newHeight;
		boolean cuttingWasNecessary = false;
		if ( existingCropRatio < targetRatio ) {
			newWidth = (int) ( targetRatio * crop.getHeight() );

			if ( newWidth <= imageWidth ) {
				newX = crop.getX() + ( crop.getWidth() - newWidth ) / 2;
				if ( newX < 0 ) {
					newX = 0;
				}
				else if ( newX + newWidth > imageWidth ) {
					newX = imageWidth - newWidth;
				}
				newHeight = crop.getHeight();
				newY = crop.getY();
			}
			else {
				newWidth = imageWidth;
				newX = 0;
				newHeight = (int) ( imageWidth / targetRatio );
				newY = crop.getY() + ( crop.getHeight() - newHeight ) / 2;
				cuttingWasNecessary = true;
			}
		}
		else if ( existingCropRatio > targetRatio ) {
			newHeight = (int) ( crop.getWidth() / targetRatio );

			if ( newHeight <= imageHeight ) {
				newY = crop.getY() + ( crop.getHeight() - newHeight ) / 2;
				if ( newY < 0 ) {
					newY = 0;
				}
				else if ( newY + newHeight > imageHeight ) {
					newY = imageHeight - newHeight;
				}
				newWidth = crop.getWidth();
				newX = crop.getX();
			}
			else {
				newHeight = imageHeight;
				newY = 0;
				newWidth = (int) ( imageHeight * targetRatio );
				newX = crop.getX() + ( crop.getWidth() - newWidth ) / 2;
				cuttingWasNecessary = true;
			}
		}
		else {
			newX = crop.getX();
			newY = crop.getY();
			newWidth = crop.getWidth();
			newHeight = crop.getHeight();
		}

		Crop newCrop = new Crop( newX, newY, newWidth, newHeight );
		float extensionMeasure = 0.0f, cutOffMeasure = 0.0f;
		if ( cuttingWasNecessary ) {
			Crop intersectingCrop = intersect( newCrop, crop );
			if ( intersectingCrop != null ) {
				float existingCropArea = area( crop );
				float intersectingCropArea = area( intersectingCrop );
				cutOffMeasure = ( existingCropArea - intersectingCropArea ) / existingCropArea;
			}
			else {
				cutOffMeasure = 1.0f;
			}
		}
		else {
			float existingCropArea = area( crop );
			float newCropArea = area( newCrop );
			extensionMeasure = ( newCropArea - existingCropArea ) / newCropArea;
		}

		float scaleFactor = (float) newWidth / (float) targetDimensions.getWidth();

		return new CropCandidate( newCrop, extensionMeasure, cutOffMeasure, scaleFactor );
	}

	private Crops obtainCrops( Image image, ImageContext context, List<ImageModification> modifications ) {
		if ( image == null || context == null || modifications == null ) {
			LOG.warn(
					"Null parameters not allowed - CropGeneratorImpl#obtainCrops: image={}, context={}, modifications={}",
					LogHelper.flatten( image, context, modifications ) );
		}

		Set<Crop> sameContext = new HashSet<>();
		Set<Crop> differentContext = new HashSet<>();

		for ( ImageModification modification : modifications ) {
			if ( modification.getContextId() == context.getId() ) {
				sameContext.add( modification.getCrop() );
			}
			else {
				differentContext.add( modification.getCrop() );
			}
		}

		/**
		 * Always consider the entire image. This also means we always have at least one crop. In the worst case
		 * scenario (i.e. when not a single crop is defined) we'll scale the entire image and do some cutting to get
		 * the required ratio.
		 */
		differentContext.add( new Crop( 0, 0, image.getDimensions().getWidth(), image.getDimensions().getHeight() ) );

		return new Crops( sameContext, differentContext );
	}

	private static class Crops
	{
		private final Set<Crop> sameContext;
		private final Set<Crop> differentContext;

		public Crops( Set<Crop> sameContext, Set<Crop> differentContext ) {
			this.sameContext = sameContext;
			this.differentContext = differentContext;
		}

		public Set<Crop> getSameContext() {
			return sameContext;
		}

		public Set<Crop> getDifferentContext() {
			return differentContext;
		}
	}

	private final static class CropCandidate
	{
		private final Crop crop;
		private final float extensionMeasure;
		private final float cutOffMeasure;
		private final float scaleFactor;

		private CropCandidate( Crop crop, float extensionMeasure, float cutOffMeasure, float scaleFactor ) {
			this.crop = crop;
			this.scaleFactor = scaleFactor;
			this.cutOffMeasure = cutOffMeasure;
			this.extensionMeasure = extensionMeasure;
		}

		public Crop getCrop() {
			return crop;
		}

		public float getExtensionMeasure() {
			return extensionMeasure;
		}

		public float getCutOffMeasure() {
			return cutOffMeasure;
		}

		public float getScaleFactor() {
			return scaleFactor;
		}
	}

}
