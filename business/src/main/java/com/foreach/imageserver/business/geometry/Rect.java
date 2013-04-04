package com.foreach.imageserver.business.geometry;

import com.foreach.imageserver.business.math.Fraction;

public class Rect implements Scaleable<Rect> {

    private final Point topLeft;
    private final Size size;

	public Rect( Size size )
	{
		this( Point.ORIGIN, size );
	}

    public Rect( Point topLeft, Size size )
    {
        this.topLeft = topLeft;
        this.size = size;
    }

    public Rect( int left, int top, int width, int height )
    {
        this.topLeft = new Point( left, top );
        this.size = new Size(width, height );
    }

    public final Point getTopLeft() {
        return topLeft;
    }

    public final Size getSize() {
        return size;
    }

    public final int getTop()
    {
        return topLeft.getY();
    }

    public final int getLeft()
    {
        return topLeft.getX();
    }

    public final int getWidth()
    {
        return size.getWidth();
    }

    public final int getHeight()
    {
        return size.getHeight();
    }

    public final int getBottom()
    {
        return topLeft.getY() + size.getHeight();
    }

    public final int getRight()
    {
        return topLeft.getX() + size.getWidth();
    }

    public final Point getBottomRight()
    {
        return new Point(  getRight(), getBottom() );
    }

    public final Rect scaleBy( Fraction f )
    {
        if( f.equals( Fraction.ONE ) ) {
	        return this;
        }

	    return new Rect( topLeft.scaleBy( f ), size.scaleBy( f ) );
    }

	public final boolean containsPoint( Point point )
	{
		int px = point.getX();
		int py = point.getY();

		return (
			( getLeft() <= px ) &&
			( px < getRight() ) &&
			( getTop() <= py ) &&
			( py < getBottom() )
		);
	}

	public final boolean withinRect( Rect outerRect )
	{
		return
				outerRect.containsPoint( topLeft ) &&
				outerRect.containsPoint( new Point( getRight()-1, getBottom()-1) );
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

        Rect other = (Rect) o;

        return ( ( size.equals( other.size ) ) && ( topLeft.equals( other.topLeft )  ) );
    }

    @Override
    public final int hashCode()
    {
        return 10007 * size.hashCode() + topLeft.hashCode();
    }
 }
