package com.foreach.imageserver.business.container;

import java.util.*;

public class CircularArrayListImpl<E> implements CircularArrayList<E>  {

    private final int capacity;
    private final List<E> backingArray;
    private int cursor = 0;

    public CircularArrayListImpl(int capacity) {
        this.capacity = capacity;
        backingArray = new ArrayList<E>( Collections.nCopies( capacity, (E) null) );
    }

    public final int getCapacity() {
        return capacity;
    }

    private void increaseCursor()
    {
        cursor = ( cursor + 1 ) % capacity;
    }

    private void write( E e )
    {
        backingArray.set( cursor, e );
    }

    // public methods

    public final synchronized void push( E e )
    {
        increaseCursor();
        write( e );
    }

    public final synchronized List<E> popAll()
    {
        List<E> result = new ArrayList<E>( capacity );
        for( int i = 0; i < capacity; i++ ) {
            E e = backingArray.get( i );
            if( e != null )  {
                result.add( e );
                backingArray.set( i, null );
            }
        }

        return result;
    }
}
