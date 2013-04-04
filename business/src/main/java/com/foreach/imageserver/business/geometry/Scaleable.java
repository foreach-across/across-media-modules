package com.foreach.imageserver.business.geometry;

import com.foreach.imageserver.business.math.Fraction;

public interface Scaleable<S>
{
    S scaleBy( Fraction fraction );
}
