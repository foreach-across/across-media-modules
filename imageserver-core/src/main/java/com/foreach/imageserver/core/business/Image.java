package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import com.foreach.imageserver.core.hibernate.ImageTypeUserType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = ImageSchemaConfiguration.TABLE_IMAGE)
public class Image
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
	private long id;

	@Column(name = "profile_id")
	private long imageProfileId;

	@Column(name = "external_id")
	private String externalId;

	@Column(name = "created")
	private Date dateCreated = new Date();

	@Column(name = "image_type_id")
	@Type(type = ImageTypeUserType.CLASS_NAME)
	private ImageType imageType;

	@AttributeOverrides({
			                    @AttributeOverride(name = "width", column = @Column(name = "width")),
			                    @AttributeOverride(name = "height", column = @Column(name = "height"))
	                    })
	private Dimensions dimensions;

	@Column(name = "file_size")
	private long fileSize;

	@Column(name = "original_path")
	private String originalPath;

	@Column(name = "variant_path")
	private String variantPath;

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId( String externalId ) {
		this.externalId = externalId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated( Date dateCreated ) {
		this.dateCreated = dateCreated;

		if ( dateCreated != null ) {
			Calendar cal = Calendar.getInstance();
			cal.setTime( dateCreated );
		}
	}

	public Dimensions getDimensions() {
		return dimensions;
	}

	public void setDimensions( Dimensions dimensions ) {
		this.dimensions = dimensions;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public void setImageType( ImageType imageType ) {
		this.imageType = imageType;
	}

	public long getImageProfileId() {
		return imageProfileId;
	}

	public void setImageProfileId( long imageProfileId ) {
		this.imageProfileId = imageProfileId;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize( long fileSize ) {
		this.fileSize = fileSize;
	}

	public String getOriginalPath() {
		return originalPath;
	}

	public void setOriginalPath( String originalPath ) {
		this.originalPath = originalPath;
	}

	public String getVariantPath() {
		return variantPath;
	}

	public void setVariantPath( String variantPath ) {
		this.variantPath = variantPath;
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

		return Objects.equals( this.id, image.id );
	}

	@Override
	public int hashCode() {
		return Objects.hash( id );
	}
}
