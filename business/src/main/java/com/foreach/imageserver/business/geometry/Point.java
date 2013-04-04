package com.foreach.imageserver.business.geometry;

import com.foreach.imageserver.business.math.Fraction;

public class Point implements Scaleable<Point> {

    private final int x;
    private final int y;

	public static final Point ORIGIN = new Point( 0, 0 );

    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public final Point scaleBy( Fraction f )
    {
	    if( f.equals( Fraction.ONE ) ) {
		    return this;
	    }

        return new Point( f.scale( x ), f.scale( y ) );
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

        Point other = (Point) o;

        return ( (x == other.x ) && ( y == other.y ) );
    }

    @Override
    public final int hashCode()
    {
        return 10007 * x + y;
    }

    @Override
    public final String toString()
    {
        return new StringBuilder()
                .append( "(")
                .append( x )
                .append( ",")
                .append( y )
                .append( ")")
                .toString();
    }
}
