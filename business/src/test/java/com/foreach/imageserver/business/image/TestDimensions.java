package com.foreach.imageserver.business.image;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.math.Fraction;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestDimensions {

    @Test
    public void absolute()
    {
        Dimensions da = new Dimensions( 400, 300 );

        assertEquals( true, da.isAbsolute() );

        Dimensions dr = new Dimensions( new Fraction( 400, 300 ) );

        assertEquals( false, dr.isAbsolute() );
    }

    @Test
    public void equality()
    {
        Dimensions da1 = new Dimensions( 400, 300 );
        Dimensions da2 = new Dimensions( 800, 600 );

        comparison(da1, da2, false);
        assertEquals( true, da1.getAspectRatio().equals( da2.getAspectRatio() ) );

        Dimensions dr1 = new Dimensions( new Fraction( 400, 300 ) );
        Dimensions dr2 = new Dimensions( new Fraction( 800, 600 ) );

        comparison(dr1, dr2, true);
        assertEquals( true, dr1.getAspectRatio().equals( dr2.getAspectRatio() ) );
    }

    private void comparison( Dimensions left, Dimensions right, boolean expected )
    {
        assertEquals( expected, right.equals( left ));
        assertEquals( expected, left.equals( right ));

        if( expected == true ) {
            assertEquals( "hashCode() is broken, it should return identical result for objects that are equal", left.hashCode(), right.hashCode());
        }
    }

    @Test
    public void matches()
    {
        Dimensions da1 = new Dimensions( 400, 300 );
        Dimensions da2 = new Dimensions( 800, 600 );
        Dimensions dr1 = new Dimensions( new Fraction( 400, 300 ) );
        Dimensions dr2 = new Dimensions( new Fraction( 800, 600 ) );

        Size size = new Size( 400, 300);

        assertEquals( true, da1.matches( size ) );
        assertEquals( false, da2.matches( size ) );
        assertEquals( true, dr1.matches( size ) );
        assertEquals( true, dr2.matches( size ) );

        size = new Size( 800, 600);

        assertEquals( false, da1.matches( size ) );
        assertEquals( true, da2.matches( size ) );
        assertEquals( true, dr1.matches( size ) );
        assertEquals( true, dr2.matches( size ) );

        size = new Size( 4, 3);

        assertEquals( false, da1.matches( size ) );
        assertEquals( false, da2.matches( size ) );
        assertEquals( true, dr1.matches( size ) );
        assertEquals( true, dr2.matches( size ) );
    }

    @Test
    public void wildCards()
    {
        Dimensions da1 = new Dimensions( 400, 0 );
        Dimensions da2 = new Dimensions( 0, 600 );
        Dimensions any = new Dimensions( 0, 0 );

        Size size = new Size( 400, 300);

        assertEquals( true, da1.matches( size ) );
        assertEquals( false, da2.matches( size ) );
        assertEquals( true, any.matches( size ) );

        size = new Size( 800, 600);

        assertEquals( false, da1.matches( size ) );
        assertEquals( true, da2.matches( size ) );
        assertEquals( true, any.matches( size ) );

        size = new Size( 400, 600);

        assertEquals( true, da1.matches( size ) );
        assertEquals( true, da2.matches( size ) );
        assertEquals( true, any.matches( size ) );

        size = new Size( 100, 200);

        assertEquals( false, da1.matches( size ) );
        assertEquals( false, da2.matches( size ) );
        assertEquals( true, any.matches( size ) );
    }

    @Test
    public void hasAspectRatio()
    {
        Dimensions da0 = new Dimensions( 400, 300 );
        Dimensions da1 = new Dimensions( 400, 0 );
        Dimensions da2 = new Dimensions( 0, 600 );
        Dimensions da3 = new Dimensions( 0, 0 );

        assertEquals( true, da0.hasAspectRatio() );
        assertEquals( false, da1.hasAspectRatio() );
        assertEquals( false, da2.hasAspectRatio() );
        assertEquals( false, da3.hasAspectRatio() );
    }
}
