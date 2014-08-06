package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Represents the profile an image is linked to.
 */
@Entity
@Table(name= ImageSchemaConfiguration.TABLE_IMAGE_PROFILE)
public class ImageProfile {

	public static final int DEFAULT_PROFILE_ID = 1;

    @Id
    @GeneratedValue( generator = "seq_img_image_profile_id" )
    @GenericGenerator(
            name = "seq_img_image_profile_id",
            strategy = AcrossSequenceGenerator.STRATEGY,
            parameters = {
                    @org.hibernate.annotations.Parameter( name = "sequenceName", value = "seq_img_image_profile_id" ),
                    @org.hibernate.annotations.Parameter( name = "allocationSize", value = "10" )
            }
    )
    private long id;

	@Column( name = "name" )
    private String name;

    public long getId() {
        return id;
    }

	public void setId( int id ) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ImageProfile ) ) {
			return false;
		}

		ImageProfile that = (ImageProfile) o;

		if ( name != null ? !name.equals( that.name ) : that.name != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}
