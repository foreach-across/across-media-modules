package com.foreach.imageserver.business.image;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.business.math.Function;
import com.foreach.imageserver.business.math.Predicate;

public class Format implements Comparable<Format>
{
	private int id;
    private int groupId;
    private String name;

    private Dimensions dimensions = new Dimensions();

    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public final int getGroupId()
	{
		return groupId;
	}

	public final void setGroupId( int groupId )
	{
		this.groupId = groupId;
	}

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final Dimensions getDimensions() {
        return dimensions;
    }

    public final void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public final boolean matches(Size size)
    {
        return dimensions.matches(size);
    }

    public final boolean hasAspectRatio()
    {
        return dimensions.hasAspectRatio();
    }

    public final Fraction getAspectRatio()
    {
        return dimensions.getAspectRatio();
    }

    public final int compareTo( Format other )
    {
        return dimensions.compareTo( other.dimensions );
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

		Format that = (Format) o;

		return groupId == that.groupId && super.equals( o );
	}

	@Override
	public final int hashCode()
	{
		int result = super.hashCode();
        result = 31 * result + id;
		result = 31 * result + groupId;
		return result;
	}

    @Override
    public final String toString()
    {
        return new StringBuffer()
               .append( name )
               .append( " ")
               .append( dimensions.toString() )
               .toString();
    }

    public static class AspectRatioFunction implements Function<Format,Fraction>
    {
        public final Fraction valueFor( Format format )
        {
            return format.getAspectRatio();
        }
    }

    public static class AbsolutePredicate implements Predicate<Format>
    {
        public final boolean appliesTo( Format format )
        {
            return format.getDimensions().isAbsolute();
        }
    }
}
