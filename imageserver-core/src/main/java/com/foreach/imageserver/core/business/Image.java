package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import com.foreach.imageserver.core.hibernate.ImageTypeUserType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = ImageSchemaConfiguration.TABLE_IMAGE)
@Getter
@Setter
public class Image extends SettableIdBasedEntity<Image>
{
	@Id
	@GeneratedValue(generator = "seq_img_image_id")
	@GenericGenerator(
			name = "seq_img_image_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_img_image_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "10")
			}
	)
	private Long id;

	@Column(name = "profile_id")
	private long imageProfileId;

	@Column(name = "external_id")
	private String externalId;

	@Column(name = "created")
	@Setter(AccessLevel.NONE)
	private Date dateCreated = new Date();

	@Column(name = "image_type_id")
	@Type(type = ImageTypeUserType.CLASS_NAME)
	private ImageType imageType;

	@AttributeOverrides({
			@AttributeOverride(name = "width", column = @Column(name = "width")),
			@AttributeOverride(name = "height", column = @Column(name = "height"))
	})
	private Dimensions dimensions;

	/**
	 * Number of scenes in the image. If supported by the image type (eg PDF, animated gif).
	 */
	@Column(name = "scene_count")
	private int sceneCount;

	@Column(name = "file_size")
	private long fileSize;

	@Column(name = "original_path")
	private String originalPath;

	@Column(name = "variant_path")
	private String variantPath;

	@Transient
	private boolean temporaryImage;

	public void setDateCreated( Date dateCreated ) {
		this.dateCreated = dateCreated;

		if ( dateCreated != null ) {
			Calendar cal = Calendar.getInstance();
			cal.setTime( dateCreated );
		}
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Image image = (Image) o;

		return temporaryImage
				? Objects.equals( this.externalId, image.externalId )
				: Objects.equals( this.id, image.id );
	}

	@Override
	public int hashCode() {
		return temporaryImage ? Objects.hash( externalId ) : Objects.hash( id );
	}

	@Override
	public String toString() {
		return "Image{" +
				"id=" + id +
				", externalId='" + externalId + '\'' +
				", temporaryImage=" + temporaryImage +
				'}';
	}
}
