package com.foreach.imageserver.business.container;


import org.junit.Test;

import static org.junit.Assert.*;

public class TestCircularArrayList {

    @Test
    public void withinCapacity()
    {
        int capacity = 100;
        CircularArrayListImpl<String> container = new CircularArrayListImpl<String>( capacity );

        for( int i = 0; i < 10; i++ )
        {
            int num = 10 * ( i + 1);

            for ( int j = 0; j < num; j++ ) {
                container.push( "foo" );
            }

            assertEquals( num, container.popAll().size() );
        }
    }

    @Test
    public void lossesExpected()
    {
        int capacity = 100;
        CircularArrayListImpl<String> container = new CircularArrayListImpl<String>( capacity );

        for( int i = 0; i < capacity + 50; i++ )
        {
            container.push( "foo" );
        }

        assertEquals( capacity, container.popAll().size() );
    }
}
