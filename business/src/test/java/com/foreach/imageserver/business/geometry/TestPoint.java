package com.foreach.imageserver.business.geometry;


import org.junit.Test;

import static org.junit.Assert.*;

public class TestPoint {

    @Test
    public void equality()
    {
        Point p = new Point( 0, 100 );
        Point q = new Point( 10, -20);
        Point q2 = new Point( 10, -20);

        assertEquals( true, p.equals( p ) );
        assertEquals( false, p.equals( q ) );
        assertEquals( false, q.equals( p ) );
        assertEquals( true, q.equals( q2 ) );
        assertEquals( true, q2.equals( q ) );
    }
}
