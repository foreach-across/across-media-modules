package com.foreach.imageserver.rendering;


import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.rendering.CroppingLogic;
import org.junit.Assert;
import org.junit.Test;

public class TestCroppingLogic {

    @Test
    public void squareInSquare()
    {
        Size sourceSize = new Size( 200, 200 );

        Rect rect = CroppingLogic.calculateCropRect( sourceSize, Fraction.ONE );

        Assert.assertEquals( 200, rect.getWidth());
        Assert.assertEquals( 200, rect.getHeight());

        // No cropping
        Assert.assertEquals( 0, rect.getLeft() );
        Assert.assertEquals( 0, rect.getTop() );
    }

    @Test
    public void rectInSameRect()
    {
        Size sourceSize = new Size( 400, 300 );

        Rect rect = CroppingLogic.calculateCropRect( sourceSize, new Fraction( 4, 3) );

        Assert.assertEquals( 400, rect.getWidth());
        Assert.assertEquals( 300, rect.getHeight());

        // No cropping
        Assert.assertEquals( 0, rect.getLeft() );
        Assert.assertEquals( 0, rect.getTop() );
    }


    @Test
    public void squareInWideRect()
    {
        Size sourceSize = new Size( 200, 100 );

        Rect rect = CroppingLogic.calculateCropRect(sourceSize, Fraction.ONE);

        Assert.assertEquals( 100, rect.getWidth());
        Assert.assertEquals( 100, rect.getHeight());

        // Cropped to the left and right
        Assert.assertEquals( 50, rect.getLeft() );
        Assert.assertEquals( 0, rect.getTop() );
    }

    @Test
    public void squareInTallRect()
    {
        Size sourceSize = new Size( 100, 250 );

        Rect rect = CroppingLogic.calculateCropRect( sourceSize, Fraction.ONE );

        Assert.assertEquals( 100, rect.getWidth());
        Assert.assertEquals( 100, rect.getHeight());

        // Cropped to the top and bottom
        Assert.assertEquals( 0, rect.getLeft() );
        Assert.assertEquals( 75, rect.getTop() );
    }

    @Test
    public void wideScreenInSquare()
    {
        Size sourceSize = new Size( 160, 160 );
        Fraction targetFraction = new Fraction( 16, 9 );

        Rect rect = CroppingLogic.calculateCropRect( sourceSize, targetFraction );

        Assert.assertEquals( 160, rect.getWidth());
        Assert.assertEquals( 90, rect.getHeight());

        // Cropped to the top and bottom
        Assert.assertEquals( 0, rect.getLeft() );
        Assert.assertEquals( 35, rect.getTop() );
    }

}
