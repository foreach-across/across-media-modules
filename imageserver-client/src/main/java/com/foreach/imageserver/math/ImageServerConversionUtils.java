package com.foreach.imageserver.math;

import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;

/**
 * Utility methods to calculate/translate DTO instances.
 *
 * @author Arne Vandamme
 */
public class ImageServerConversionUtils
{
	private ImageServerConversionUtils() {
	}

	public static AspectRatio calculateAspectRatio( DimensionsDto dimensions ) {
		return new AspectRatio( dimensions.getWidth(), dimensions.getHeight() );
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
	 * The largest dimension according to aspect ratio is kept.
	 *
	 * @param original    dimensions
	 * @param aspectRatio to use when normalizing the dimensions
	 * @return normalized dimensions
	 */
	public static DimensionsDto normalize( DimensionsDto original, AspectRatio aspectRatio ) {
		DimensionsDto normalized = new DimensionsDto( original.getWidth(), original.getHeight() );

		if ( !calculateAspectRatio( normalized ).equals( aspectRatio ) ) {
			if ( aspectRatio.getNumerator() > aspectRatio.getDenominator() ) {
				normalized.setHeight( aspectRatio.calculateHeightForWidth( normalized.getWidth() ) );
			}
			else {
				normalized.setWidth( aspectRatio.calculateWidthForHeight( normalized.getHeight() ) );
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
		if ( original.getSource() == null || original.getSource().equals( new DimensionsDto() )) {
			throw new IllegalArgumentException( "Normalizing a crop requires a valid source to be set." );
		}

		CropDto translated = new CropDto( original );

		// Translate the crop coordinates to fit with the new box
		translateSource( translated, scaleToFitIn( original.getSource(), box ) );
		translated.setBox( box );

		// Ensure the resulting crop is valid
		if ( translated.getWidth() == 0 || translated.getHeight() == 0 ) {
			throw new RuntimeException( "Could not process crop: resulted in an illegal width or height of 0" );
		}

		return translated;
	}

	private static void translateSource( CropDto crop, DimensionsDto newDimensions ) {
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
}
