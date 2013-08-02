package com.foreach.imageserver.business;

/**
 * Specifies a single set of modifications to be done to an image.
 */
public class ImageModifier
{
	public static final ImageModifier EMPTY = new ImageModifier();
	private static final ImageModifier EMPTY_WITH_STRETCH;

	static {
		EMPTY_WITH_STRETCH = new ImageModifier();
		EMPTY_WITH_STRETCH.setStretch( true );
	}

	private int width, height;
	private Crop crop = new Crop();
	private ImageType output;
	private boolean stretch;
	private boolean keepAspect;
	private Dimensions density = new Dimensions();

	public int getWidth() {
		return width;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public Crop getCrop() {
		return crop;
	}

	public void setCrop( Crop crop ) {
		this.crop = crop;
	}

	public ImageType getOutput() {
		return output;
	}

	public void setOutput( ImageType output ) {
		this.output = output;
	}

	public boolean isStretch() {
		return stretch;
	}

	public void setStretch( boolean stretch ) {
		this.stretch = stretch;
	}

	public boolean isKeepAspect() {
		return keepAspect;
	}

	public void setKeepAspect( boolean keepAspect ) {
		this.keepAspect = keepAspect;
	}

	/**
	 * Density is the lowest possible numbers to be used as multiplier on the original
	 * as to achieve the ideal original dimensions for the output requested.
	 *
	 * @return Horizontal (width) and vertical (height) density.
	 */
	public Dimensions getDensity() {
		return density;
	}

	public void setDensity( Dimensions density ) {
		this.density = density;
	}

	public void setDensity( int density ) {
		setDensity( density, density );
	}

	public void setDensity( int horizontal, int vertical ) {
		density.setWidth( horizontal );
		density.setHeight( vertical );
	}

	public boolean isOnlyDimensions() {
		ImageModifier other = new ImageModifier();
		other.setStretch( stretch );
		other.setKeepAspect( keepAspect );
		other.setWidth( width );
		other.setHeight( height );

		return this.equals( other );
	}

	public boolean isEmpty() {
		return this.equals( EMPTY ) || this.equals( EMPTY_WITH_STRETCH );
	}

	public boolean hasCrop() {
		return crop != null && !crop.isEmpty();
	}

	/**
	 * Will normalize the modifier based on the dimensions of the original passed in.
	 *
	 * @param dimensions Dimensions of the original image.
	 * @return Normalized ImageModifier fitting the original image.
	 */
	public ImageModifier normalize( Dimensions dimensions ) {
		if ( isEmpty() ) {
			return new ImageModifier();
		}

		ImageModifier normalized = new ImageModifier();
		adjustCrop( normalized, dimensions );
		adjustWidthAndHeight( normalized, dimensions );
		normalized.setOutput( output );
		normalized.setDensity( density );

		calculateDensity( normalized, dimensions );

		return normalized;
	}

	private void calculateDensity( ImageModifier normalized, Dimensions original ) {
		if ( Dimensions.EMPTY.equals( normalized.getDensity() ) ) {
			Dimensions calculated = new Dimensions();

			int requestedWidth = normalized.getWidth();
			int requestedHeight = normalized.getHeight();

			int originalWidth = normalized.hasCrop() ? normalized.getCrop().getWidth() : original.getWidth();
			int originalHeight = normalized.hasCrop() ? normalized.getCrop().getHeight() : original.getHeight();

			if ( originalWidth >= requestedWidth ) {
				calculated.setWidth( 1 );
			}
			else {
				calculated.setWidth( Double.valueOf( Math.ceil( requestedWidth / (double) originalWidth ) ).intValue() );
			}
			if ( originalHeight >= requestedHeight ) {
				calculated.setHeight( 1 );
			}
			else {
				calculated.setHeight( Double.valueOf( Math.ceil( requestedHeight / (double) originalHeight ) ).intValue() );
			}

			normalized.setDensity( calculated );
		}
	}

	private void adjustCrop( ImageModifier normalized, Dimensions dimensions ) {
		if ( crop != null && !crop.isEmpty() ) {
			if ( dimensions != null && dimensions.getWidth() > 0 && dimensions.getHeight() > 0 ) {
				Crop normalizedCrop = crop.normalize( dimensions );

				if ( !dimensions.equals( new Dimensions( normalizedCrop.getWidth(), normalizedCrop.getHeight() ) ) ) {
					normalized.setCrop( normalizedCrop );
				}
			}
			else {
				normalized.setCrop( new Crop( crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight() ) );
			}
		}
	}

	private void adjustWidthAndHeight( ImageModifier normalized, Dimensions dimensions ) {
		normalized.setStretch( stretch );

		Dimensions maxDimensions = normalized.hasCrop() ? normalized.getCrop().getDimensions() : dimensions;

		Dimensions dimensionsToUse = new Dimensions();
		dimensionsToUse.setWidth( width );
		dimensionsToUse.setHeight( height );

		if ( maxDimensions != null && maxDimensions.getWidth() > 0 && maxDimensions.getHeight() > 0 ) {
			dimensionsToUse = dimensionsToUse.normalize( maxDimensions );

			if ( keepAspect ) {
				dimensionsToUse = dimensionsToUse.normalize( maxDimensions.getAspectRatio() );
			}
			if ( !stretch ) {
				dimensionsToUse = dimensionsToUse.scaleToFitIn( maxDimensions );
			}
		}

		normalized.setWidth( dimensionsToUse.getWidth() );
		normalized.setHeight( dimensionsToUse.getHeight() );
	}

	@SuppressWarnings( "all" )
	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageModifier modifier = (ImageModifier) o;

		if ( height != modifier.height ) {
			return false;
		}
		if ( stretch != modifier.stretch ) {
			return false;
		}
		if ( width != modifier.width ) {
			return false;
		}
		if ( crop != null ? !crop.equals( modifier.crop ) : modifier.crop != null ) {
			return false;
		}
		if ( output != modifier.output ) {
			return false;
		}

		if ( density != null && !Dimensions.EMPTY.equals(
				density ) && modifier.density != null && !Dimensions.EMPTY.equals( modifier.density ) ) {
			return density.equals( modifier.density );
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = width;
		result = 31 * result + height;
		result = 31 * result + ( crop != null ? crop.hashCode() : 0 );
		result = 31 * result + ( output != null ? output.hashCode() : 0 );
		result = 31 * result + ( stretch ? 1 : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "ImageModifier{" +
				"width=" + width +
				", height=" + height +
				", crop=" + crop +
				", output=" + output +
				", stretch=" + stretch +
				'}';
	}
}
