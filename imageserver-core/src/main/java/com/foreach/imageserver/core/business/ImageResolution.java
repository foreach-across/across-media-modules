package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import com.foreach.imageserver.core.hibernate.ImageTypeSetUserType;
import com.foreach.imageserver.core.hibernate.TagsUserType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.*;

/**
 * <p>An ImageResolution specifies a permitted output resolution. Every Application has an associated list of
 * ImageResolution-s for which ImageModification-s can be registered.</p>
 * <p>
 * Note that width and height are nullable. When a dimension is set explicitly, associated ImageModification-s should
 * adhere to it exactly. When a dimension is NULL, however, we expect the ImageModification to vary it so that the
 * aspect ratio of the original image is maintained.</p>
 * <p>
 * For specifying the actual dimensions of an image, see Dimensions.
 * </p>
 * <p>Configurable means that a crop can be configured explicitly for the resolution.  A non-configurable resolution
 * can still be requested, but will not be offered for manual crop configuration.</p>
 * <p>ImageResolution name is optional and can be used to provide a more meaningful description to a (mostly
 * configurable) resolution, eg. Large teaser format.</p>
 */
@Entity
@Table(name = ImageSchemaConfiguration.TABLE_IMAGE_RESOLUTION)
@Getter
@Setter
public class ImageResolution extends SettableIdBasedEntity<ImageResolution>
{
	@Id
	@GeneratedValue(generator = "seq_img_image_resolution_id")
	@GenericGenerator(
			name = "seq_img_image_resolution_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_img_image_resolution_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "10")
			}
	)
	private Long id;

	@Column(name = "width")
	private int width;

	@Column(name = "height")
	private int height;

	@ManyToMany(fetch = FetchType.EAGER)
	@BatchSize(size = 50)
	@JoinTable(
			name = ImageSchemaConfiguration.TABLE_CONTEXT_IMAGE_RESOLUTION,
			joinColumns = @JoinColumn(name = "image_resolution_id"),
			inverseJoinColumns = @JoinColumn(name = "context_id"))
	private Collection<ImageContext> contexts = new TreeSet<>();

	@Column(name = "configurable")
	private boolean configurable;

	@Column(name = "pregenerate")
	private boolean pregenerateVariants;

	@Column(name = "name")
	@Getter(AccessLevel.NONE)
	private String name;

	@Column(name = "tags")
	@Type(type = TagsUserType.CLASS_NAME)
	private Set<String> tags = new HashSet<>();

	@Column(name = "output_types", nullable = false)
	@Type(type = ImageTypeSetUserType.CLASS_NAME)
	private Set<ImageType> allowedOutputTypes = EnumSet.noneOf( ImageType.class );

	public Dimensions getDimensions() {
		return new Dimensions( getWidth(), getHeight() );
	}

	public String getName() {
		return StringUtils.isBlank( name ) ? generatedName() : name;
	}

	private String generatedName() {

		if ( width == 0 && height == 0 ) {
			return "original";
		}
		else if ( width == 0 ) {
			return "H" + height;
		}
		else if ( height == 0 ) {
			return "W" + width;
		}
		else {
			return width + "x" + height;
		}
	}

	public boolean isAllowedOutputType( ImageType imageType ) {
		return getAllowedOutputTypes().contains( imageType );
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageResolution that = (ImageResolution) o;

		return Objects.equals( this.id, that.id );
	}

	@Override
	public int hashCode() {
		return Objects.hash( id );
	}
}
