package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.services.DtoUtil;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageTransformDto;
import com.foreach.imageserver.math.AspectRatio;
import com.foreach.imageserver.math.ImageServerConversionUtils;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * Replaces the {@link com.foreach.imageserver.core.services.CropGeneratorUtil}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Component
public class ImageTransformUtils
{
	/**
	 * Normalize a transform to be executable for an image with the given attributes.
	 * This will calculate all unknown dimensions and simplify components of the transform (eg perform crop coordinate normalization).
	 *
	 * @param transformDto     transform
	 * @param sourceAttributes of the image to which the transform should apply
	 * @return normalized transform
	 */
	public ImageTransformDto normalize( @NonNull ImageTransformDto transformDto, @NonNull ImageAttributes sourceAttributes ) {
		ImageTransformDto normalized = transformDto.toBuilder().build();

		calculateWidthAndHeight( normalized, transformDto, sourceAttributes.getDimensions() );
		adjustWithAndHeightToFitInMax( normalized, transformDto, sourceAttributes.getDimensions() );

		CropDto crop = transformDto.getCrop();

		if ( crop != null ) {
			normalizeCrop( normalized, transformDto, sourceAttributes.getDimensions() );
		}
		else {
			calculateDefaultCrop( normalized, sourceAttributes.getDimensions() );
		}

		return normalized;
	}

	private void calculateWidthAndHeight( ImageTransformDto normalized, ImageTransformDto original, Dimensions actualDimensions ) {
		DimensionsDto widthAndHeight = calculateDimensions( original.getWidth(), original.getHeight(), actualDimensions, original.getAspectRatio() );

		if ( widthAndHeight != null ) {
			normalized.setWidth( widthAndHeight.getWidth() );
			normalized.setHeight( widthAndHeight.getHeight() );
		}

		normalized.setAspectRatio( null );
	}

	private void adjustWithAndHeightToFitInMax( ImageTransformDto normalized, ImageTransformDto original, Dimensions actualDimensions ) {
		DimensionsDto maxWidthAndHeight = calculateDimensions(
				normalized.getMaxWidth(), normalized.getMaxHeight(), actualDimensions, original.getAspectRatio()
		);

		if ( maxWidthAndHeight != null ) {
			DimensionsDto requested = outputDimensions( normalized, actualDimensions );

			if ( !ImageServerConversionUtils.fitsIn( requested, maxWidthAndHeight ) ) {
				requested = ImageServerConversionUtils.scaleToFitIn( requested, maxWidthAndHeight );
				normalized.setWidth( requested.getWidth() );
				normalized.setHeight( requested.getHeight() );
			}
		}

		normalized.setMaxWidth( null );
		normalized.setMaxHeight( null );
	}

	private DimensionsDto calculateDimensions( Integer width, Integer height, Dimensions inputDimensions, AspectRatio requestedAspectRatio ) {
		DimensionsDto requested = new DimensionsDto( defaultToZero( width ), defaultToZero( height ) );

		if ( !requested.isEmpty() ) {
			if ( requested.hasUnspecifiedDimension() ) {
				return requestedAspectRatio != null
						? ImageServerConversionUtils.normalize( requested, requestedAspectRatio )
						: ImageServerConversionUtils.normalize( requested, DtoUtil.toDto( inputDimensions ) );
			}
			else {
				return requested;
			}
		}
		else if ( requestedAspectRatio != null ) {
			DimensionsDto original = DtoUtil.toDto( inputDimensions );
			DimensionsDto originalAccordingToAspectRatio = ImageServerConversionUtils.normalize( original, requestedAspectRatio );

			if ( !original.equals( originalAccordingToAspectRatio ) ) {
				return ImageServerConversionUtils.scaleToFitIn( originalAccordingToAspectRatio, original );
			}
		}

		return null;
	}

	private void normalizeCrop( ImageTransformDto normalized, ImageTransformDto original, Dimensions inputDimensions ) {
		CropDto crop = new CropDto( original.getCrop() );

		// Adjust source dimensions based on original aspect ratio if necessary
		Dimensions source = DtoUtil.toBusiness( crop.getSource() ).normalize( inputDimensions );

		if ( crop.hasBox() ) {
			// Adjust box dimensions based on original aspect ratio if necessary
			Dimensions box = DtoUtil.toBusiness( crop.getBox() ).normalize( inputDimensions );

			// Ensure the source dimensions fit in the box
			source = source.scaleToFitIn( box );
		}

		// Box is no longer relevant as source is set
		crop.setBox( new DimensionsDto() );
		crop.setSource( DtoUtil.toDto( source ) );

		// Translate the crop coordinates to fit with the actual original and make sure the crop is valid
		applyCropSourceToCrop( crop, inputDimensions );

		if ( crop.getWidth() == 0 || crop.getHeight() == 0 ) {
			throw new IllegalArgumentException( "Could not process crop: resulted in an illegal width or height of 0" );
		}

		normalized.setCrop( crop );
	}

	private void applyCropSourceToCrop( CropDto crop, Dimensions originalDimensions ) {
		int leftX = crop.getX();
		int leftY = crop.getY();
		int rightX = leftX + crop.getWidth();
		int rightY = leftY + crop.getHeight();

		DimensionsDto source = crop.getSource();

		leftX = snap( leftX, source.getWidth() );
		leftY = snap( leftY, source.getHeight() );
		rightX = snap( rightX, source.getWidth() );
		rightY = snap( rightY, source.getHeight() );

		if ( !originalDimensions.equals( DtoUtil.toBusiness( source ) ) ) {
			double modX = (double) originalDimensions.getWidth() / source.getWidth();
			double modY = (double) originalDimensions.getHeight() / source.getHeight();

			crop.setX( Double.valueOf( leftX * modX ).intValue() );
			crop.setY( Double.valueOf( leftY * modY ).intValue() );
			crop.setWidth( Double.valueOf( ( rightX - leftX ) * modX ).intValue() );
			crop.setHeight( Double.valueOf( ( rightY - leftY ) * modY ).intValue() );
		}
		else {
			crop.setX( leftX );
			crop.setY( leftY );
			crop.setWidth( rightX - leftX );
			crop.setHeight( rightY - leftY );
		}

		crop.setSource( new DimensionsDto() );
	}

	private int snap( int pos, int max ) {
		if ( pos < 0 ) {
			return 0;
		}
		else if ( pos > max ) {
			return max;
		}
		return pos;
	}

	private void calculateDefaultCrop( ImageTransformDto normalized, Dimensions inputDimensions ) {
		DimensionsDto targetDimensions = outputDimensions( normalized, inputDimensions );
		AspectRatio targetRatio = DtoUtil.toBusiness( targetDimensions ).fetchAspectRatio();
		if ( !inputDimensions.fetchAspectRatio().equals( targetRatio ) ) {
			int width = inputDimensions.getWidth();
			int height = inputDimensions.getHeight();

			if ( targetRatio.isLargerOnWidth() ) {
				height = targetRatio.calculateHeightForWidth( width );
			}
			else {
				width = targetRatio.calculateWidthForHeight( height );
			}

			DimensionsDto cropDimensions = ImageServerConversionUtils.scaleToFitIn(
					new DimensionsDto( width, height ),
					new DimensionsDto( inputDimensions.getWidth(), inputDimensions.getHeight() )
			);
			int x = ( inputDimensions.getWidth() - cropDimensions.getWidth() ) / 2;
			int y = ( inputDimensions.getHeight() - cropDimensions.getHeight() ) / 2;
			normalized.setCrop( new CropDto( x, y, cropDimensions.getWidth(), cropDimensions.getHeight() ) );
		}
	}

	private DimensionsDto outputDimensions( ImageTransformDto transform, Dimensions actualDimensions ) {
		DimensionsDto requested = new DimensionsDto( defaultToZero( transform.getWidth() ), defaultToZero( transform.getHeight() ) );

		if ( requested.isEmpty() ) {
			return DtoUtil.toDto( actualDimensions );
		}

		return requested;
	}

	private int defaultToZero( Integer value ) {
		return value == null || value <= 0 ? 0 : value;
	}
}
