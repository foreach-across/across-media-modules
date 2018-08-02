package com.foreach.imageserver.core.business;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

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
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Entity
@Table(name = ImageSchemaConfiguration.TABLE_IMAGE_MODIFICATION)
@Getter
@Setter
public class ImageModification implements Persistable<ImageModificationId>, Serializable
{
	@EmbeddedId
	private ImageModificationId id;

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

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageModification that = (ImageModification) o;

		if ( !id.equals( that.id ) ) {
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
		return Objects.hash( id, crop, density );
	}

	@Override
	public boolean isNew() {
		return id != null;
	}

	public long getImageId() {
		return id.getImageId();
	}

	public long getContextId() {
		return id.getContextId();
	}

	public long getResolutionId() {
		return id.getResolutionId();
	}

	public void setImageId( long imageId ) {
		id.setImageId( imageId );
	}

	public void setContextId( long contextId ) {
		id.setContextId( contextId );
	}

	public void setResolutionId( long resolutionId ) {
		id.setResolutionId( resolutionId );
	}
}
