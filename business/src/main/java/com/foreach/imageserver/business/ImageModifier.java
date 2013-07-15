package com.foreach.imageserver.business;

/**
 * Specifies a single set of modifications to be done to an image.
 */
public class ImageModifier
{
	public static final ImageModifier EMPTY = new ImageModifier();

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
