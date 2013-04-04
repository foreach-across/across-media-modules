package com.foreach.imageserver.rendering;


import com.foreach.imageserver.business.geometry.Size;

public interface QualityControl {

    boolean mayScale( Size sourceSize, Size targetSize );
}
