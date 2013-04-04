package com.foreach.imageserver.business.taxonomy;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.math.Fraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group
{
	private int id;
    private int applicationId;
	private String name;

	private List<Format> formats = new ArrayList<Format>();

	public final int getId()
	{
		return id;
	}

	public final void setId( int id )
	{
		this.id = id;
	}

	public final int getApplicationId()
	{
		return applicationId;
	}

	public final void setApplicationId( int applicationId )
	{
		this.applicationId = applicationId;
	}

	public final String getName()
	{
		return name;
	}

	public final void setName( String name )
	{
		this.name = name;
	}

	public final List<Format> getFormats()
	{
		return formats;
	}

	public final void setFormats( List<Format> formats )
	{
		this.formats = formats;
	}

	/**
	 * Checks if a given format request is allowed for this group.
	 * A format request is always specified by a combination of width and height.
	 * Allowed formats are configured as with/height or aspect ratio.
	 *
	 * @param size Requested size.
	 * @return True if a format matching the requested size exists for this group, false if not.
	 */
	public final boolean isFormatAllowed( Size size )
	{
        for ( Format format: formats ) {
			if ( format.matches( size ) ) {
			   return true;
			}
		}
		return false;
	}

    // Return all aspect ratios used in the formats for this group sorted by their natural order
    public final List<Fraction> getAllAspectRatios()
    {
        List<Fraction> result = new ArrayList<Fraction>();

        for( Format format : formats ) {
            if( format.hasAspectRatio() ) {
                Fraction ratio = format.getAspectRatio();
                if ( !result.contains( ratio ) ) {
                    result.add( ratio );
                }
            }
        }

        Collections.sort( result );
        return result;
    }
}
