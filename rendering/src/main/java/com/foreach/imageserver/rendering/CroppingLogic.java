package com.foreach.imageserver.rendering;

import com.foreach.imageserver.business.geometry.Point;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.math.Fraction;

public final class CroppingLogic {

    private CroppingLogic()
    {

    }

    // Given a sourceSize and a target ratio, create a crop rect

    public static Rect calculateCropRect( Size sourceSize, Fraction targetRatio )
    {
        Fraction sourceRatio = new Fraction( sourceSize.getWidth(), sourceSize.getHeight() );

        Fraction rel = targetRatio.divideBy( sourceRatio );

        int compare = rel.compareTo( Fraction.ONE );

        if( compare == 0 ) {
            // identical ratios, no cropping
            return new Rect( new Point( 0, 0 ), sourceSize );
        }

        int width = sourceSize.getWidth();
        int height = sourceSize.getHeight();

        if( compare > 0 ) {
            //source ratio < target ratio, crop from top and bottom
            int targetHeight = rel.deScale( height );
            int dy = ( height - targetHeight ) / 2;

            return new Rect( new Point( 0, dy ), new Size( width, targetHeight ) );
        } else {
            // source ratio > target ratio , crop from left and right
            int targetWidth = rel.scale( width );
            int dx = ( width - targetWidth ) / 2;

            return new Rect( new Point( dx, 0 ), new Size( targetWidth, height ) );
        }
    }
}
