package com.foreach.imageserver.business.math;

public interface Function<V,W> {

    W valueFor( V v );
}
