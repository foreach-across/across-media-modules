package com.foreach.imageserver.rendering;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.rendering.QualityControl;
import com.foreach.imageserver.rendering.QualityControlImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestQualityControl {

    private QualityControl qualityControl;

    @Before
    public void prepareForTest() {

        qualityControl = new QualityControlImpl();
    }

    @Test
    public void expectedFailures()
    {
        Size source = new Size( 120, 120 );

        Size target1 = new Size( 80, 140 );
        Size target2 = new Size( 150, 100 );
        Size target3 = new Size( 200, 170 );

        Assert.assertEquals( false, qualityControl.mayScale( source, target1 ) );
        Assert.assertEquals( false, qualityControl.mayScale( source, target2 ) );
        Assert.assertEquals( false, qualityControl.mayScale( source, target3 ) );
    }

    @Test
    public void ok()
    {
        Size source = new Size( 120, 120 );

        Size target1 = new Size( 80, 100 );
        Size target2 = new Size( 120, 100 );
        Size target3 = new Size( 60, 40 );

        Assert.assertEquals( true, qualityControl.mayScale( source, target1 ) );
        Assert.assertEquals( true, qualityControl.mayScale( source, target2 ) );
        Assert.assertEquals( true, qualityControl.mayScale( source, target3 ) );

        // borderline case
        Assert.assertEquals( true, qualityControl.mayScale( source, source ) );
    }
}
