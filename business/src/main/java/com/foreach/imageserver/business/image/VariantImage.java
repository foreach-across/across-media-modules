package com.foreach.imageserver.business.image;

import org.apache.commons.lang.time.FastDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class VariantImage {
    private long id;
	private long imageId;
    private int formatId;
    private Long cropId;
    private int width;
    private int height;
    private int version;
	private Date lastCalled;
    private Date dateCreated;

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final long getImageId() {
        return imageId;
    }

    public final void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public final int getFormatId() {
        return formatId;
    }

    public final void setFormatId(int formatId) {
        this.formatId = formatId;
    }

    public final Long getCropId() {
        return cropId;
    }

    public final void setCropId(Long cropId) {
        this.cropId = cropId;
    }

    public final int getVersion() {
        return version;
    }

    public final void setVersion(int version) {
        this.version = version;
    }

    public final Date getLastCalled() {
        return lastCalled;
    }

    public final void setLastCalled(Date lastCalled) {
        this.lastCalled = lastCalled;
    }

    public final Date getDateCreated() {
        return dateCreated;
    }

    public final void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        this.width = width;
    }

    public final int getHeight() {
        return height;
    }

    public final void setHeight(int height) {
        this.height = height;
    }

    public final boolean hasCropId() {
        return cropId != null;
    }

    public final String toString()
    {
        StringBuilder sb = new StringBuilder();
        FastDateFormat dateFormat = FastDateFormat.getInstance( "yyyy-MM-dd hh:mm:ss", TimeZone.getDefault(), Locale.getDefault() );

        sb.append("imageId: ");
        sb.append(this.imageId);
        sb.append( " " );
        sb.append("formatId: ");
        sb.append( this.formatId );
        sb.append( " " );
        if( hasCropId() ) {
            sb.append("cropId: ");
            sb.append( this.cropId );
            sb.append( " " );
        }
        sb.append("dimensions: ");
        sb.append( this.width );
        sb.append( "px x" );
        sb.append( this.height );
        sb.append( "px " );
        sb.append("version: ");
        sb.append( this.version );
        sb.append( " " );
        if (lastCalled != null){
            sb.append("last called: ");
            sb.append( dateFormat.format(this.lastCalled) );
            sb.append( " " );
        }
        if (dateCreated != null){
            sb.append("created: ");
            sb.append( dateFormat.format(this.dateCreated) );
        }

        return sb.toString();
    }

	@Override
	public final boolean equals( Object o )
	{
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		if ( !super.equals( o ) ) {
			return false;
		}

		VariantImage other = (VariantImage) o;

        if ( id != other.id ) {
            return false;
        }
        if ( imageId != other.imageId ) {
            return false;
        }
		if ( formatId != other.formatId ) {
			return false;
		}
        if ( width != other.width ) {
			return false;
		}
        if ( height != other.height ) {
			return false;
		}

		if ( version != other.version ) {
			return false;
		}

		return super.equals( o );
	}

	@Override
	public final int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + Long.valueOf(id).intValue();
		result = 31 * result + Long.valueOf(imageId).intValue();
        result = 31 * result + Long.valueOf(formatId).intValue();
        result = 31 * result + ( ( cropId == null ) ? 0 : cropId.hashCode() );
		result = 31 * result + version;
        result = 31 * result + Long.valueOf(width).intValue();
        result = 31 * result + Long.valueOf(height).intValue();

		return result;
	}
}
