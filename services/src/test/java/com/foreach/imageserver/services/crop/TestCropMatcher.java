package com.foreach.imageserver.services.crop;

import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TestCropMatcher
{

    private CropMatcher matcher;

    private Set<Crop> crops;

    private Crop crop_16by9_v0;
    private Crop crop_16by9_v0_160;
    private Crop crop_4by3_v0_400;
    private Crop crop_4by3_v0_800;
    private Crop crop_4by3_v1_800;
    private Crop crop_4by3_v1;

    @Before
    public void matching()
    {
        matcher = new CropMatcherImpl();

        // A basic case: A generic crop for 4/3, and a specific crop for 4/3 width 160
        crop_16by9_v0 = cropFrom( 0, 0, 320, 180, 16, 9, 0, 0 );
        crop_16by9_v0_160 = cropFrom( 0, 0, 320, 180, 16, 9, 0, 160 );

        // No generic case defined here, only widths 400 and 800 for ratio 4/3
        crop_4by3_v0_400 = cropFrom( 0, 0, 400, 300, 4, 3, 0, 400 );
        crop_4by3_v0_800 = cropFrom( 0, 0, 800, 600, 4, 3, 0, 800 );

        // version1 for 4/3
        crop_4by3_v1 = cropFrom( 0, 0, 400, 300, 4, 3, 1, 0 );
        crop_4by3_v1_800 = cropFrom( 0, 0, 400, 300, 4, 3, 1, 800 );

        crops = new HashSet<Crop>();

        crops.add( crop_16by9_v0 );
        crops.add( crop_16by9_v0_160 );
        crops.add( crop_4by3_v0_400 );
        crops.add( crop_4by3_v0_800 );
        crops.add( crop_4by3_v1_800 );
        crops.add( crop_4by3_v1 );
    }

    private Crop cropFrom( int x, int y, int width, int height,
                           int ratioWidth, int ratioHeight, int version, int targetWidth )
    {
        Crop crop = new Crop();

        crop.setCropRect( new Rect( x, y, width, height ) );
        crop.setRatioWidth( ratioWidth );
        crop.setRatioHeight( ratioHeight );
        crop.setVersion( version );
        crop.setTargetWidth( targetWidth );

        return crop;
    }

    @Test
    public void matchingTargetWidth()
    {
        // We have two version 0 crops with aspectRatio 4/3 but different targetWidths,
        // check if the correct one is returned.

        Size sizerequested = new Size( 800, 600 );
        Crop match = matcher.bestCropFrom( crops, 0, sizerequested );
        Assert.assertSame( crop_4by3_v0_800, match );

        sizerequested = new Size( 400, 300 );
        match = matcher.bestCropFrom( crops, 0, sizerequested );
        Assert.assertSame( crop_4by3_v0_400, match );

        // there's no generic 4/3 for v0, so expect null
        sizerequested = new Size( 200, 150 );
        match = matcher.bestCropFrom( crops, 0, sizerequested );
        Assert.assertSame( null, match );

    }

    @Test
    public void forceVersion()
    {
        // no specific crop defined for this version, aspect ratio and target width,
        // so fall back to the crop matching version and aspect ratio
        Size sizerequested = new Size( 800, 600 );
        Crop match = matcher.bestCropFrom( crops, 1, sizerequested );
        Assert.assertSame( crop_4by3_v1_800, match );

        // return size-specific crop
        sizerequested = new Size( 400, 300 );
        match = matcher.bestCropFrom( crops, 1, sizerequested );
        Assert.assertSame( crop_4by3_v1, match );
    }

    @Test
    public void bestRelative()
    {
        Size sizerequested = new Size( 16, 9 );
        Crop match = matcher.bestCropFrom( crops, 0, sizerequested );
        Assert.assertSame( crop_16by9_v0, match );

        sizerequested = new Size( 160, 90 );
        match = matcher.bestCropFrom( crops, 0, sizerequested );
        Assert.assertSame(crop_16by9_v0_160, match);

        // version 1 or higher falls through to version 0
        match = matcher.bestCropFrom( crops, 1, sizerequested );
        Assert.assertSame(crop_16by9_v0_160, match);

        match = matcher.bestCropFrom( crops, 2, sizerequested );
        Assert.assertSame(crop_16by9_v0_160, match);
    }

    @Test
    public void notFound()
    {
        Size sizerequested = new Size( 3141592, 1000000 );
        Crop match = matcher.bestCropFrom( crops, 0, sizerequested );
        Assert.assertNull( match );
    }
}
