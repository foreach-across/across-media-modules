package com.foreach.imageserver.business.geometry;


import org.junit.Test;
import static org.junit.Assert.*;

public class TestSize {

    @Test
    public void equality()
    {
        Size size1 = new Size( 10, 10);
        Size size2 = new Size( 10, 20);
        Size size3 = new Size( 15, 20);
        Size size1b = new Size( 10, 10);

        assertEquals( true, size1.equals( size1 ) );
        assertEquals( false, size1.equals( size2 ) );
        assertEquals( false, size1.equals( size3 ) );
        assertEquals( true, size1.equals( size1b ) );
        assertEquals( true, size1b.equals( size1 ) );
    }

    @Test
    public void scale()
    {
        Size original = new Size( 150, 100 );

        Size scaled = original.scaleIfWider( 150 );
        assertEquals( original, scaled );

        scaled = original.scaleIfWider( 100 );
        assertEquals( new Size( 100, 66 ), scaled );

        scaled = original.scaleIfHigher( 100 );
        assertEquals( original, scaled );

        scaled = original.scaleIfHigher( 70 );
        assertEquals( new Size( 105, 70 ), scaled );
    }

    @Test
    public void proportional()
    {
        Size size1 = new Size( 150, 100 );
        Size size2 = new Size( 30, 20 );
        Size size3 = new Size( 30, 30 );

        assertEquals( true, size1.isProportionalTo( size2 ) );
        assertEquals( true, size2.isProportionalTo( size1 ) );
        assertEquals( false, size1.isProportionalTo( size3 ) );
    }
}
