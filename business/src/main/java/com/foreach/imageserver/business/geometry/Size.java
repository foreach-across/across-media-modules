package com.foreach.imageserver.business.geometry;

import com.foreach.imageserver.business.math.Fraction;

public class Size implements Scaleable<Size> {

    private final int width;
    private final int height;

    public Size( int width, int height )
    {
        this.width = width;
        this.height = height;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final Fraction aspectRatio()
    {
        return new Fraction( width, height );
    }

    public final Size scaleBy( Fraction f )
    {
	    if( f.equals( Fraction.ONE ) ) {
		    return this;
	    }

        return new Size( f.scale( width ), f.scale( height ) );
    }

    public final Size scaleIfHigher( int maxHeight )
    {
        if ( height > maxHeight ) {
            return new Size( ( width * maxHeight ) / height , maxHeight );
        } else {
            return this;
        }
    }

    public final Size scaleIfWider( int maxWidth )
    {
        if ( width > maxWidth ) {
            return new Size( maxWidth, ( height * maxWidth ) / width );
        } else {
            return this;
        }
    }

    public final boolean isProportionalTo( Size other )
    {
        long lhs = (long) width * other.height;
	    long rhs = (long) height * other.width;
	    return lhs == rhs;
    }

    // If the arguments are proportional, return the size of the receiver relative to the argument
    public final Fraction relativeSize( Size base )
    {
        if( ! isProportionalTo( base ) ) {
            throw new ArithmeticException("no division possible, "+base.toString()+" not proportional to "+this.toString() );
        }

        return new Fraction( width, base.width );
    }

    public final Fraction relativeSizeUnchecked( Size base )
    {
        return new Fraction( width, base.width );
    }

    @Override
    public final boolean equals( Object o )
    {
        if( this == o ) {
            return true;
        }

        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        Size other = (Size) o;

        return ( (width == other.width) && ( height == other.height ) );
    }

    @Override
    public final int hashCode()
    {
        return 10007 * width + height;
    }

    @Override
    public final String toString()
    {
        return new StringBuilder()
                .append( width )
                .append( "x")
                .append( height )
                .toString();
    }
}
