package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import com.foreach.imageserver.dto.ImageModificationDto;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

/**
 * Specific default modification attached to an image profile.
 */
@Entity
@Table(name= ImageSchemaConfiguration.TABLE_IMAGE_PROFILE_MODIFICATION)
public class ImageProfileModification {
    @Id
    @GeneratedValue( generator = "seq_img_image_profile_modification_id" )
    @GenericGenerator(
            name = "seq_img_image_profile_modification_id",
            strategy = AcrossSequenceGenerator.STRATEGY,
            parameters = {
                    @org.hibernate.annotations.Parameter( name = "sequenceName", value = "seq_img_image_profile_modification_id" ),
                    @org.hibernate.annotations.Parameter( name = "allocationSize", value = "10" )
            }
    )
    private long id;

    @Column( name = "resolution_id" )
    private long imageResolutionId;

    @Column( name = "profile_id" )
    private long imageProfileId;

    @Column( name = "context_id" )
    private long imageContextId;

	@Transient //TODO: FIX
    private ImageModificationDto modificationDto = new ImageModificationDto();

	public long getImageResolutionId() {
		return imageResolutionId;
	}

	public void setImageResolutionId( long imageResolutionId ) {
		this.imageResolutionId = imageResolutionId;
	}

	public long getImageProfileId() {
		return imageProfileId;
	}

	public void setImageProfileId( long imageProfileId ) {
		this.imageProfileId = imageProfileId;
	}

	public long getImageContextId() {
		return imageContextId;
	}

	public void setImageContextId( long imageContextId ) {
		this.imageContextId = imageContextId;
	}

	public ImageModificationDto getModificationDto() {
		return modificationDto;
	}

	public void setModificationDto( ImageModificationDto modificationDto ) {
		this.modificationDto = modificationDto;
	}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageProfileModification that = (ImageProfileModification) o;

        return Objects.equals( this.id, that.id );
    }

    @Override
    public int hashCode() {
        return Objects.hash( id );
    }
}
