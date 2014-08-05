package com.foreach.imageserver.core.business;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The ImageModification specifies how an original image is to be transformed into an image conforming to a specific
 * ImageResolution.
 * <p/>
 * Note that an ImageModification is generic; the non-generic options required for an actual transform are specified
 * using an ImageVariant object.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public class ImageModification
{
	private int imageId;
	private int contextId;
	private int resolutionId;
	private Crop crop;
	private Dimensions density;

	public int getImageId() {
		return imageId;
	}

	public void setImageId( int imageId ) {
		this.imageId = imageId;
	}

	public int getContextId() {
		return contextId;
	}

	public void setContextId( int contextId ) {
		this.contextId = contextId;
	}

	public int getResolutionId() {
		return resolutionId;
	}

	public void setResolutionId( int resolutionId ) {
		this.resolutionId = resolutionId;
	}

	public Crop getCrop() {
		return crop;
	}

	public void setCrop( Crop crop ) {
		this.crop = crop;
	}

	public Dimensions getDensity() {
		return density;
	}

	public void setDensity( Dimensions density ) {
		this.density = density;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageModification that = (ImageModification) o;

		if ( contextId != that.contextId ) {
			return false;
		}
		if ( imageId != that.imageId ) {
			return false;
		}
		if ( resolutionId != that.resolutionId ) {
			return false;
		}
		if ( crop != null ? !crop.equals( that.crop ) : that.crop != null ) {
			return false;
		}
		if ( density != null ? !density.equals( that.density ) : that.density != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = imageId;
		result = 31 * result + contextId;
		result = 31 * result + resolutionId;
		result = 31 * result + ( crop != null ? crop.hashCode() : 0 );
		result = 31 * result + ( density != null ? density.hashCode() : 0 );
		return result;
	}
}