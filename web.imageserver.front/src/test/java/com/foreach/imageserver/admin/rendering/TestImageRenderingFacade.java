package com.foreach.imageserver.admin.rendering;


import com.foreach.imageserver.business.geometry.Point;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.rendering.ImageResizer;
import com.foreach.imageserver.services.crop.CropMatcher;
import org.junit.Before;
import org.junit.Test;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.mockito.Mockito.*;


public class TestImageRenderingFacade {

    private ImageRenderingFacade renderer;
    private ImagePathBuilder pathBuilder;
    private ImageResizer imageResizer;
    private CropMatcher cropMatcher;

    @Before
    public void setup()
    {
        renderer = new ImageRenderingFacadeImpl();

        pathBuilder = mock( ImagePathBuilder.class );
        imageResizer = mock( ImageResizer.class );
        cropMatcher = mock( CropMatcher.class );

        inject( renderer, "pathBuilder", pathBuilder );
        inject( renderer, "imageResizer", imageResizer );
        inject( renderer, "cropMatcher", cropMatcher );
    }

    @Test
    public void render() throws Exception
    {
        int width = 400;
        int height = 300;
        int version = 0;

        String srcPath = "src.jpg";
        String dstPath = "dst.jpg";

        ServableImageData data = new ServableImageData();
        data.setWidth( width );
        data.setHeight( height );

        ImageSpecifier imageSpecifier = new ImageSpecifier();
        imageSpecifier.setWidth( width );
        imageSpecifier.setHeight( height );
        imageSpecifier.setVersion( version );

        Rect rect = new Rect( new Point( 0, 0), new Size( width, height ) );
        Crop crop = new Crop();
        crop.setCropRect( rect );

        when(pathBuilder.generateOriginalImagePath(data)).thenReturn( srcPath );
        when( pathBuilder.generateVariantImagePath( data, imageSpecifier ) ).thenReturn( dstPath );

        when( cropMatcher.bestCropFrom( anySet(), anyInt(),(Size) anyObject())).thenReturn( crop );

        renderer.generateVariant( data, imageSpecifier );

        verify( imageResizer, times( 1 ) ).resize( srcPath, dstPath, new Size( width, height ),
                new Rect( new Point( 0, 0), new Size( width, height )) );
    }

}
