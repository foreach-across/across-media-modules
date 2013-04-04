package com.foreach.imageserver.rendering;

import com.foreach.imageserver.business.geometry.Size;

public class QualityControlImpl implements QualityControl {

    // TODO-pjs(20110629): implement other constraints on scaling:
    // - on image distortion ( if vertical scale differs from horizontal scale )

    public final boolean mayScale( Size sourceSize, Size targetSize )
    {
        return (
                ( targetSize.getWidth() <= sourceSize.getWidth() ) &&
                ( targetSize.getHeight() <= sourceSize.getHeight() )
        );
    }
}
