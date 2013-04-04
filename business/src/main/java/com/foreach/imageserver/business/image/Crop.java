package com.foreach.imageserver.business.image;

import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.business.math.Function;

public class Crop
{
    private long id;
	private long imageId;
    private int version;

    private Rect cropRect = null;
    private int ratioWidth;
    private int ratioHeight;

    private int targetWidth;

    public final long getId() {
        return id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final long getImageId()
	{
		return imageId;
	}

	public final void setImageId( long imageId )
	{
		this.imageId = imageId;
	}

    public final int getVersion()
	{
		return version;
	}

	public final void setVersion( int version )
	{
		this.version = version;
	}

    public final Size getSize()
    {
        return cropRect.getSize();
    }

    public final void setCropRect(Rect cropRect)
    {
        this.cropRect = cropRect;
    }

    public final Rect getCropRect()
    {
        return cropRect;
    }

    public final int getRatioWidth()
    {
        return ratioWidth;
    }

    public final void setRatioWidth( int ratioWidth )
    {
        this.ratioWidth = ratioWidth;
    }

    public final int getRatioHeight()
    {
        return ratioHeight;
    }

    public final void setRatioHeight( int ratioHeight )
    {
        this.ratioHeight = ratioHeight;
    }

    public final int getTargetWidth() {
        return targetWidth;
    }

    public final void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public final boolean hasAspectRatio()
    {
        return ratioHeight != 0;
    }

    public final Fraction getAspectRatio()
    {
        return new Fraction( ratioWidth, ratioHeight );
    }

	public final boolean withinRect( Rect rect )
	{
		return cropRect.withinRect( rect );
	}

    public final String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("crop( offset( ");
        sb.append( cropRect.getLeft() );
        sb.append( ", " );
        sb.append( cropRect.getTop() );
        sb.append( "), size( " );
        sb.append( cropRect.getWidth() );
        sb.append( "," );
        sb.append( cropRect.getHeight() );
        sb.append( ") ) " );

        if( hasAspectRatio() ) {
            sb.append( " [" );
            sb.append( getAspectRatio().toString() );
            sb.append( "]" );
        }
        sb.append( " v" );
        sb.append( version );
        sb.append( " - " );
        sb.append( targetWidth );
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

		Crop other = (Crop) o;

        if ( id != other.id ) {
            return false;
        }
        if ( imageId != other.imageId ) {
            return false;
        }
		if ( cropRect != other.cropRect ) {
			return false;
		}
		if ( version != other.version ) {
			return false;
		}
        if ( targetWidth != other.targetWidth ) {
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
		result = 31 * result + version;
        result = 31 * result + targetWidth;
		result = 31 * result + ( ( cropRect == null ) ? 0 : cropRect.hashCode() );
		return result;
	}

    public static class VersionFunction implements Function<Crop,Integer>
    {
        public final Integer valueFor( Crop crop )
        {
            return crop.version;
        }
    }
}
