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

		return normalized;
	}

	private void adjustCrop( ImageModifier normalized, Dimensions dimensions ) {
		if ( crop != null && !crop.isEmpty() ) {
			if ( dimensions != null && dimensions.getWidth() > 0 && dimensions.getHeight() > 0 ) {
				Crop normalizedCrop = crop.normalize( dimensions );

				if ( !dimensions.equals( new Dimensions( normalizedCrop.getWidth(), normalizedCrop.getHeight() ) ) ) {
					normalized.setCrop(
							new Crop( normalizedCrop.getX(), normalizedCrop.getY(), normalizedCrop.getWidth(),
							          normalizedCrop.getHeight() ) );
					normalized.setWidth( normalizedCrop.getWidth() );
					normalized.setHeight( normalizedCrop.getHeight() );
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

		int width = this.width == 0 ? normalized.getWidth() : this.width;
		int height = this.height == 0 ? normalized.getHeight() : this.height;

		if ( maxDimensions != null && maxDimensions.getWidth() > 0 && maxDimensions.getHeight() > 0 ) {
			Fraction originalAspectRatio = maxDimensions.getAspectRatio();

			if ( width == 0 && height == 0 ) {
				width = maxDimensions.getWidth();
				height = maxDimensions.getHeight();
			}
			else if ( height == 0 ) {
				height = originalAspectRatio.calculateHeightForWidth( width );
			}
			else if ( width == 0 ) {
				width = originalAspectRatio.calculateWidthForHeight( height );
			}

			if ( !stretch ) {
				Fraction requestedAspectRatio = new Dimensions( width, height ).getAspectRatio();

				if ( width > maxDimensions.getWidth() ) {
					width = maxDimensions.getWidth();
					height = requestedAspectRatio.calculateHeightForWidth( width );
				}

				if ( height > maxDimensions.getHeight() ) {
					height = maxDimensions.getHeight();
					width = requestedAspectRatio.calculateWidthForHeight( height );
				}
			}
		}

		normalized.setWidth( width );
		normalized.setHeight( height );
	}

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
