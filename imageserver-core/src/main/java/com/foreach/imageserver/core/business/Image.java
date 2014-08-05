package com.foreach.imageserver.core.business;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name= ImageSchemaConfiguration.TABLE_IMAGE)
public class Image {

    @Id
    @GeneratedValue( generator = "seq_img_image_id" )
    @GenericGenerator(
            name = "seq_img_image_id",
            strategy = AcrossSequenceGenerator.STRATEGY,
            parameters = {
                    @org.hibernate.annotations.Parameter( name = "sequenceName", value = "seq_img_image_id" ),
                    @org.hibernate.annotations.Parameter( name = "allocationSize", value = "10" )
            }
    )
    private long id;

    @Column( name = "profile_id" )
    private int imageProfileId;

    @Column( name = "external_id" )
    private String externalId;

    @Column( name = "created" )
    private Date dateCreated;

    @Column( name = "width" )
    private int width;

    @Column( name = "height" )
    private int height;

    @Column( name = "image_type_id" )
    @Type(type = ImageTypeUserType.CLASS_NAME)
    private ImageType imageType;

	@Transient //TODO: FIX
	private Dimensions dimensions;

	@Transient //TODO: FIX
	private String dateCreatedYearString;
	@Transient //TODO: FIX
	private String dateCreatedMonthString;
	@Transient //TODO: FIX
	private String dateCreatedDayString;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

			dateCreatedYearString = Integer.toString( cal.get( Calendar.YEAR ) );
			dateCreatedMonthString = StringUtils.leftPad( Integer.toString( cal.get( Calendar.MONTH ) + 1 ), 2, '0' );
			dateCreatedDayString = StringUtils.leftPad( Integer.toString( cal.get( Calendar.DAY_OF_MONTH ) ), 2, '0' );
		}
		else {
			dateCreatedYearString = null;
			dateCreatedMonthString = null;
			dateCreatedDayString = null;
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

	public String getDateCreatedYearString() {
		return dateCreatedYearString;
	}

	public String getDateCreatedMonthString() {
		return dateCreatedMonthString;
	}

	public String getDateCreatedDayString() {
		return dateCreatedDayString;
	}

	public int getImageProfileId() {
		return imageProfileId;
	}

	public void setImageProfileId( int imageProfileId ) {
		this.imageProfileId = imageProfileId;
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