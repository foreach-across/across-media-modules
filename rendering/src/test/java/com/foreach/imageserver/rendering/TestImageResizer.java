package com.foreach.imageserver.rendering;


import com.foreach.imageserver.business.geometry.Point;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.rendering.ImageResizerImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring-config-rendering-test.xml" })
public class TestImageResizer {


    @Autowired
    ImageResizerImpl imageMagick;

    @Test
    public void imageMagickConfigured()
    {
        String s = imageMagick.getPath();
        Assert.assertEquals(false, s == null);
        Assert.assertEquals( false, s.equals( "" ) );
    }

    @Test
    public void imageMagickInstalled() throws Exception
    {
        imageMagick.version();
    }

    private String pathPrefix()
    {
        File file = new File("foo");
        if( file.getAbsolutePath().contains("rendering")) {
            return "";
        } else {
            return "rendering/";
        }
    }

    @Test
    public void getSize() throws Exception
    {
        Size expectedSize = new Size( 3000, 2008 );
        Size size = imageMagick.getSize( pathPrefix()+"src/test/resources/img/theroad_3000x2008.jpg");
        Assert.assertEquals( expectedSize, size );
    }

    @Test(expected=IOException.class)
    public void missingFile() throws Exception
    {
        imageMagick.getSize( "Kneel_before_Zod.jpg" );
    }

    @Test
    public void scale() throws Exception
    {
        int targetWidth = 300;
        int targetHeight = 201;

        Size expectedSize = new Size( targetWidth, targetHeight );

        String sourcePath = pathPrefix()+"src/test/resources/img/theroad_3000x2008.jpg";
        String targetPath = pathPrefix()+"src/test/resources/img/theroad_300x201.jpg";
        imageMagick.resize( sourcePath, targetPath, expectedSize, null );

        Size size = imageMagick.getSize( targetPath );
        Assert.assertEquals( expectedSize, size );

        new File( targetPath ).delete();
    }

    @Test
    public void crop() throws Exception
    {
        int targetWidth = 1000;
        int targetHeight = 600;
        int left = 400;
        int top = 600;

        Size expectedSize = new Size( targetWidth, targetHeight );
        Rect cropRect = new Rect( new Point( left, top ), expectedSize );

        String sourcePath = pathPrefix()+"src/test/resources/img/theroad_3000x2008.jpg";
        String targetPath = pathPrefix()+"src/test/resources/img/theroad_cropped.jpg";
        imageMagick.resize( sourcePath, targetPath, null, cropRect );

        Size size = imageMagick.getSize( targetPath );
        Assert.assertEquals( expectedSize, size );
        new File( targetPath ).delete();
    }

    @Test
    public void cropAndResize() throws Exception
    {
        int cropWidth = 1000;
        int cropHeight = 600;
        int left = 400;
        int top = 600;
        int targetWidth = 500;
        int targetHeight = 300;

        Size cropSize = new Size( cropWidth, cropHeight );
        Rect cropRect = new Rect( new Point( left, top ), cropSize );
        Size expectedSize = new Size( targetWidth, targetHeight );

        String sourcePath = pathPrefix()+"src/test/resources/img/theroad_3000x2008.jpg";
        String targetPath = pathPrefix()+"src/test/resources/img/theroad_cropped_500x300.jpg";
        imageMagick.resize( sourcePath, targetPath, expectedSize, cropRect );

        Size size = imageMagick.getSize( targetPath );
        Assert.assertEquals( expectedSize, size );
        new File( targetPath ).delete();
    }
}
