package com.foreach.imageserver.business.math;

public interface Predicate<V> {

    boolean appliesTo( V v );
}
