package com.foreach.imageserver.math;

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
	 * Will downscale the dimensions to fit in the boundaries if they are larger.
	 *
	 * @param original   dimensions
	 * @param boundaries that the translated dimensions should fit in
	 * @return translated dimensions
	 */
	public static DimensionsDto scaleToFitIn( DimensionsDto original, DimensionsDto boundaries ) {
		DimensionsDto normalized = normalize( original, boundaries );
		DimensionsDto scaled = new DimensionsDto( normalized.getWidth(), normalized.getHeight() );

		AspectRatio aspectRatio = calculateAspectRatio( normalized );

		if ( !fitsIn( normalized, boundaries ) ) {
			if ( aspectRatio.isLargerOnWidth() ) {
				scaled.setWidth( boundaries.getWidth() );
				scaled.setHeight( aspectRatio.calculateHeightForWidth( boundaries.getWidth() ) );
			}
			else {
				scaled.setHeight( boundaries.getHeight() );
				scaled.setWidth( aspectRatio.calculateWidthForHeight( boundaries.getHeight() ) );
			}

			if ( !fitsIn( scaled, boundaries ) ) {
				// Reverse the side as scaling basis, we made the wrong decision
				if ( aspectRatio.isLargerOnWidth() ) {
					scaled.setHeight( boundaries.getHeight() );
					scaled.setWidth( aspectRatio.calculateWidthForHeight( boundaries.getHeight() ) );
				}
				else {
					scaled.setWidth( boundaries.getWidth() );
					scaled.setHeight( aspectRatio.calculateHeightForWidth( boundaries.getWidth() ) );

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
