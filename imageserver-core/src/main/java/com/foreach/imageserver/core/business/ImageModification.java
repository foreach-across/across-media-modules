package com.foreach.imageserver.core.business;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * The ImageModification specifies how an original image is to be transformed into an image conforming to a specific
 * ImageResolution.
 * <p/>
 * Note that an ImageModification is generic; the non-generic options required for an actual transform are specified
 * using an ImageVariant object.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
@Entity
@Table(name = ImageSchemaConfiguration.TABLE_IMAGE_MODIFICATION)
public class ImageModification implements Serializable
{
	@Id
	@Column( name = "image_id" )
	private long imageId;
	@Id
	@Column( name = "context_id" )
	private long contextId;
	@Id
	@Column( name = "resolution_id" )
	private long resolutionId;

	@AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "cropX")),
            @AttributeOverride(name = "y", column = @Column(name = "cropY")),
            @AttributeOverride(name = "width", column = @Column(name = "cropWidth")),
			@AttributeOverride(name = "height", column = @Column(name = "cropHeight"))
    })
	private Crop crop;

	@AttributeOverrides({
            @AttributeOverride(name = "width", column = @Column(name = "densityWidth")),
            @AttributeOverride(name = "height", column = @Column(name = "densityHeight"))
    })
	private Dimensions density;

	public long getImageId() {
		return imageId;
	}

	public void setImageId( long imageId ) {
		this.imageId = imageId;
	}

	public long getContextId() {
		return contextId;
	}

	public void setContextId( long contextId ) {
		this.contextId = contextId;
	}

	public long getResolutionId() {
		return resolutionId;
	}

	public void setResolutionId( long resolutionId ) {
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
		return Objects.hash( imageId, contextId, resolutionId, crop, density );
	}
}
