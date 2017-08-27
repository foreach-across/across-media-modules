package com.foreach.imageserver.math;

import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.springframework.util.Assert;

/**
 * Utility methods to calculate/translate DTO instances.
 *
 * @author Arne Vandamme
 */
public class ImageServerConversionUtils
{
	private ImageServerConversionUtils() {
	}

	public static AspectRatio calculateAspectRatio( ImageResolutionDto resolutionDto ) {
		return calculateAspectRatio( resolutionDto.getDimensions() );
	}

	public static AspectRatio calculateAspectRatio( DimensionsDto dimensions ) {
		return new AspectRatio( dimensions.getWidth(), dimensions.getHeight() );
	}

	/**
	 * This will extend an existing crop to match a given aspect ratio.
	 * The center point of the crop will be kept, so extending will happen equally on all sides involved.
	 * This can return a crop with negative x and y coordinates.
	 * Use {@link #moveToFit(CropDto, DimensionsDto)} if you also want the crop to be moved about to fit it
	 * in the visible coordinates box.
	 * <p/>
	 * Crop parameters like source and box will be kept.
	 *
	 * @param original    crop coordinates
	 * @param aspectRatio the crop should match
	 * @return modified crop
	 * @see #moveToFit(CropDto, DimensionsDto)
	 */
	public static CropDto extendCrop( CropDto original, AspectRatio aspectRatio ) {
		CropDto cropDto = new CropDto( original );
		DimensionsDto cropDimensions = cropDto.getDimensions();
		cropDimensions = normalize( cropDimensions, aspectRatio );

		cropDto.setX( cropDto.getX() - ( cropDimensions.getWidth() - cropDto.getWidth() ) / 2 );
		cropDto.setY( cropDto.getY() - ( cropDimensions.getHeight() - cropDto.getHeight() ) / 2 );
		cropDto.setWidth( cropDimensions.getWidth() );
		cropDto.setHeight( cropDimensions.getHeight() );

		return cropDto;
	}

	/**
	 * This will shrink an existing crop to match a given aspect ratio.
	 * The center point of the crop will be kept.  Assuming that the original crop had valid coordinates,
	 * this will result in a valid crop.
	 * <p/>
	 * Crop parameters like source and box will be kept.
	 *
	 * @param original    crop coordinates
	 * @param aspectRatio the crop should match
	 * @return modified crop
	 */
	public static CropDto shrinkCrop( CropDto original, AspectRatio aspectRatio ) {
		CropDto cropDto = new CropDto( original );
		DimensionsDto originalDimensions = cropDto.getDimensions();
		DimensionsDto cropDimensions = normalize( originalDimensions, aspectRatio );
		cropDimensions = scaleToFitIn( cropDimensions, originalDimensions );

		cropDto.setX( cropDto.getX() + ( cropDto.getWidth() - cropDimensions.getWidth() ) / 2 );
		cropDto.setY( cropDto.getY() + ( cropDto.getHeight() - cropDimensions.getHeight() ) / 2 );
		cropDto.setWidth( cropDimensions.getWidth() );
		cropDto.setHeight( cropDimensions.getHeight() );

		return cropDto;
	}

	/**
	 * Will shift a crop to fit entirely inside the box.  Usually used after {@link #extendCrop(CropDto, AspectRatio)}
	 * to verify that the resulting extension does not surpass the box boundaries.  This counters the center point
	 * based extending of a crop.
	 * <p/>
	 * This function will throw an exception if it is not possible to fit the crop inside the box.
	 *
	 * @param original crop coordinates
	 * @param box      the crop should fit in
	 * @return modified crop
	 * @throws IllegalArgumentException if the crop dimensions are impossible to fit in the box
	 */
	public static CropDto moveToFit( CropDto original, DimensionsDto box ) {
		CropDto cropDto = new CropDto( original );

		if ( !fitsIn( cropDto.getDimensions(), box ) ) {
			throw new IllegalArgumentException(
					"Crop dimensions are larger than the box dimensions - impossible to fit" );
		}

		if ( cropDto.getX() < 0 ) {
			cropDto.setX( 0 );
		}
		if ( cropDto.getY() < 0 ) {
			cropDto.setY( 0 );
		}
		if ( cropDto.getX() + cropDto.getWidth() > box.getWidth() ) {
			cropDto.setX( box.getWidth() - cropDto.getWidth() );
		}
		if ( cropDto.getY() + cropDto.getHeight() > box.getHeight() ) {
			cropDto.setY( box.getHeight() - cropDto.getHeight() );
		}

		return cropDto;
	}

	/**
	 * Will calculate the unknown dimensions for the original according to the boundaries specified.
	 * Any unknown dimensions will be scaled according to the aspect ratio of the boundaries.
	 *
	 * @param original   dimensions
	 * @param boundaries for the new dimensions
	 * @return normalized dimensions
	 */
	public static DimensionsDto normalize( DimensionsDto original, DimensionsDto boundaries ) {
		DimensionsDto normalized = new DimensionsDto( original.getWidth(), original.getHeight() );

		AspectRatio originalAspectRatio = calculateAspectRatio( boundaries );

		if ( original.getWidth() == 0 && original.getHeight() == 0 ) {
			normalized.setWidth( boundaries.getWidth() );
			normalized.setHeight( boundaries.getHeight() );
		}
		else if ( original.getHeight() == 0 ) {
			normalized.setHeight( originalAspectRatio.calculateHeightForWidth( original.getWidth() ) );
		}
		else if ( original.getWidth() == 0 ) {
			normalized.setWidth( originalAspectRatio.calculateWidthForHeight( original.getHeight() ) );
		}

		return normalized;
	}

	/**
	 * Will verify and modify the dimensions so that they match the aspect ratio.
	 * The resulting dimensions will never be smaller than the original one and always
	 * one of the original dimensions will be kept.
	 *
	 * @param original    dimensions
	 * @param aspectRatio to use when normalizing the dimensions
	 * @return normalized dimensions
	 */
	public static DimensionsDto normalize( DimensionsDto original, AspectRatio aspectRatio ) {
		DimensionsDto normalized = new DimensionsDto( original.getWidth(), original.getHeight() );

		if ( !calculateAspectRatio( normalized ).equals( aspectRatio ) ) {
			int newWidth = aspectRatio.calculateWidthForHeight( normalized.getHeight() );

			if ( newWidth < normalized.getWidth() ) {
				normalized.setHeight( aspectRatio.calculateHeightForWidth( normalized.getWidth() ) );
			}
			else {
				normalized.setWidth( newWidth );
			}
		}

		return normalized;
	}

	/**
	 * Will translate a crop to fit inside the box specified.
	 * This requires the source dimensions of the crop to be set.
	 *
	 * @param original crop
	 * @param box      to use for translating the crop coordinates
	 * @return normalized crop
	 */
	public static CropDto normalize( CropDto original, DimensionsDto box ) {
		if ( original.getSource() == null || original.getSource().equals( new DimensionsDto() ) ) {
			throw new IllegalArgumentException( "Normalizing a crop requires a valid source to be set." );
		}

		CropDto translated = new CropDto( original );

		// Translate the crop coordinates to fit with the new box
		DimensionsDto newSource = scaleToFitIn( original.getSource(), box );
		translateCropToNewSource( translated, newSource );
		translated.setSource( newSource );
		translated.setBox( box );

		// Ensure the resulting crop is valid
		if ( translated.getWidth() == 0 || translated.getHeight() == 0 ) {
			throw new RuntimeException( "Could not process crop: resulted in an illegal width or height of 0" );
		}

		return translated;
	}

	private static void translateCropToNewSource( CropDto crop, DimensionsDto newDimensions ) {
		int leftX = crop.getX();
		int leftY = crop.getY();
		int rightX = leftX + crop.getWidth();
		int rightY = leftY + crop.getHeight();

		DimensionsDto source = crop.getSource();

		leftX = snap( leftX, source.getWidth() );
		leftY = snap( leftY, source.getHeight() );
		rightX = snap( rightX, source.getWidth() );
		rightY = snap( rightY, source.getHeight() );

		if ( !newDimensions.equals( source ) ) {
			double modX = (double) newDimensions.getWidth() / source.getWidth();
			double modY = (double) newDimensions.getHeight() / source.getHeight();

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
	}

	private static int snap( int pos, int max ) {
		if ( pos < 0 ) {
			return 0;
		}
		else if ( pos > max ) {
			return max;
		}
		return pos;
	}

	/**
	 * Will downscale the dimensions to fit in the boundaries if they are larger.
	 *
	 * @param original dimensions
	 * @param box      that the translated dimensions should fit in
	 * @return translated dimensions
	 */
	public static DimensionsDto scaleToFitIn( DimensionsDto original, DimensionsDto box ) {
		DimensionsDto normalized = normalize( original, box );
		DimensionsDto scaled = new DimensionsDto( normalized.getWidth(), normalized.getHeight() );

		AspectRatio aspectRatio = calculateAspectRatio( normalized );

		if ( !fitsIn( normalized, box ) ) {
			if ( aspectRatio.isLargerOnWidth() ) {
				scaled.setWidth( box.getWidth() );
				scaled.setHeight( aspectRatio.calculateHeightForWidth( box.getWidth() ) );
			}
			else {
				scaled.setHeight( box.getHeight() );
				scaled.setWidth( aspectRatio.calculateWidthForHeight( box.getHeight() ) );
			}

			if ( !fitsIn( scaled, box ) ) {
				// Reverse the side as scaling basis, we made the wrong decision
				if ( aspectRatio.isLargerOnWidth() ) {
					scaled.setHeight( box.getHeight() );
					scaled.setWidth( aspectRatio.calculateWidthForHeight( box.getHeight() ) );
				}
				else {
					scaled.setWidth( box.getWidth() );
					scaled.setHeight( aspectRatio.calculateHeightForWidth( box.getWidth() ) );

				}
			}
		}

		return scaled;
	}

	/**
	 * Checks if the area represented by dimensions fits inside the area defined by the box (the original
	 * area should be the same surface or smaller).
	 *
	 * @param original   dimensions to check
	 * @param boundaries that the dimensions should fit in
	 * @return true if dimensions fit
	 */
	public static boolean fitsIn( DimensionsDto original, DimensionsDto boundaries ) {
		return original.getWidth() <= boundaries.getWidth() && original.getHeight() <= boundaries.getHeight();
	}

	/**
	 * Checks if a crop is entirely positioned within a box.  The crop dimensions should be smaller or equal
	 * to the box dimensions, and the left top coordinates should not be negative.
	 *
	 * @param crop to check
	 * @param box  that should contain the crop
	 * @return true if crop fits
	 */
	public static boolean isWithinBox( CropDto crop, DimensionsDto box ) {
		return crop.getX() >= 0 && crop.getY() >= 0
				&& fitsIn( crop.getDimensions(), box )
				&& ( crop.getX() + crop.getWidth() ) <= box.getWidth()
				&& ( crop.getY() + crop.getHeight() ) <= box.getHeight();
	}

	public static int calculateDistance( ImageResolutionDto from, ImageResolutionDto to ) {
		return calculateDistance( from.getDimensions(), to.getDimensions() );
	}

	/**
	 * Calculates the distance between two dimensions.  If the dimensions are exactly the same, the distance is 0.
	 * If the first dimensions fits entirely in the second, the distance will be positive.  The larger the second
	 * dimension, the greater the distance number.  If the first dimension is larger than the second dimension, the
	 * distance number will be negative.  Distances closer to 0 indicate how much smaller the scond dimension is.
	 * <p/>
	 * A dimension is considered smaller as soon as a single side is smaller.  Calculating the distance is
	 * impossible if any of the arguments have an unknown dimension.
	 * <p/>
	 * The actual distance number is the delta between the two surfaces.  If the surface is the same (but dimensions
	 * are reversed), then the distance will be -1.
	 *
	 * @param from dimension
	 * @param to   dimension
	 * @return distance number
	 */
	public static int calculateDistance( DimensionsDto from, DimensionsDto to ) {
		Assert.notNull( from );
		Assert.notNull( to );
		Assert.isTrue( from.getWidth() > 0 && from.getHeight() > 0 && to.getWidth() > 0 && to.getHeight() > 0 );

		if ( from.equals( to ) ) {
			return 0;
		}

		if ( fitsIn( from, to ) ) {
			return Math.abs( ( to.getWidth() * to.getHeight() ) - ( from.getWidth() * from.getHeight() ) );
		}

		int distance = -Math.abs( ( to.getWidth() * to.getHeight() ) - ( from.getWidth() * from.getHeight() ) );

		return distance == 0 ? -1 : distance;

	}
}
