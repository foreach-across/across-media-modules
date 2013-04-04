package com.foreach.imageserver.admin.controllers;


import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.admin.models.CropUploadModel;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.Dimensions;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.math.Fraction;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.services.GroupService;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.crop.CropMatcher;
import com.foreach.imageserver.services.crop.CropService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.mockito.Mockito.*;

public class TestCropController {

    private CropController controller;

    private ImageService imageService;
    private GroupService groupService;
    private CropService cropService;
    private ImagePathBuilder pathBuilder;
    private CropMatcher cropMatcher;

    private int imageId = 1001;
    private int groupId = 2002;
    private int formatId = 3003;

    private ServableImageData image;
    private Group group;

	long crop4by3Id = 4004;
	long crop16by9Id = 4005;

	private CropUploadModel crop4by3;
	private CropUploadModel crop16by9;


    @Before
    public void setup()
    {
        controller = new CropController();

        imageService = mock( ImageService.class );
        groupService = mock( GroupService.class );
        cropService = mock( CropService.class );
        pathBuilder = mock( ImagePathBuilder.class );
        cropMatcher = mock( CropMatcher.class );

        inject( controller, "imageService", imageService );
        inject( controller, "groupService", groupService );
        inject( controller, "cropService", cropService );
        inject( controller, "pathBuilder", pathBuilder );
        inject( controller, "cropMatcher", cropMatcher );

        imageId = 1001;

        image = new ServableImageData();
        image.setId( imageId );
        image.setGroupId( groupId );
        image.setWidth( 1600 );
        image.setHeight( 900 );

        List<Format> formats = new ArrayList<Format>();
        Format format = new Format();
        format.setId( formatId );
        format.setGroupId( groupId );
        Dimensions dimensions = new Dimensions();
        dimensions.setRatio( new Fraction( 16, 9 ) );
        format.setDimensions( dimensions );
        formats.add( format );

        group = new Group();
        group.setId( groupId );
        group.setFormats( formats );

        when( imageService.getImageById( imageId ) ).thenReturn( image );

        when( groupService.getGroupById( groupId ) ).thenReturn( group );

	    crop4by3 = new CropUploadModel();
	    crop4by3.setCropId( crop4by3Id );
	    crop4by3.setImageId(imageId);
	    crop4by3.setWidth( 200 );   // these are in relative coordinates, ie. max 400 width, and height scaled proportional
	    crop4by3.setHeight( 150 );
	    crop4by3.setLeft( 10 );
	    crop4by3.setTop( 40 );

	    crop16by9 = new CropUploadModel();
	    crop16by9.setCropId( crop16by9Id );
	    crop16by9.setImageId(imageId);
	    crop16by9.setWidth( 160 );
	    crop16by9.setHeight( 90 );
	    crop16by9.setLeft( 350 );
	    crop16by9.setTop( 100 );

    }

    @Test
    public void retrieve()
    {
        Fraction ratio = new Fraction( 16,9 );
        int width = 800;
        int version = 3;

        controller.cropWithRatioWidthAndVersion( imageId, ratio, width, version, true );

        verify( cropMatcher, times( 1) ).bestCropFrom( image.getCrops() , version, ratio, width );
    }

    @Test
    public void update()
    {
        Fraction aspectRatio = new Fraction( 4, 3);

        controller.updateCrop(imageId, aspectRatio, 0, 0, crop4by3, null);

        verify( cropService, times( 1 ) ).saveCrop( (Crop) anyObject());
    }

    @Test
    public void aspectRatioDoubleSave()
    {
        int version = 2;

        Fraction aspectRatio = new Fraction( 4, 3);

        when( cropService.getCrop( imageId, aspectRatio, 0, 0 ) ).thenReturn( null );

        controller.updateCrop(imageId, aspectRatio, 0, version, crop4by3, null);

        verify( cropService, times( 1 ) ).getCrop( imageId, aspectRatio, 0, 0 );
        verify( cropService, times( 2 ) ).saveCrop( (Crop) anyObject());
    }

    @Test
    public void aspectRatioSingleSave()
    {
        int version = 2;

        Fraction aspectRatio = new Fraction( 4, 3);

        when( cropService.getCrop( imageId, aspectRatio, 0, 0 ) ).thenReturn( new Crop() );

        controller.updateCrop(imageId, aspectRatio, 0, version, crop4by3, null);

        verify( cropService, times( 1 ) ).getCrop( imageId, aspectRatio, 0, 0 );
        verify( cropService, times( 1 ) ).saveCrop( (Crop) anyObject());
    }

    @Test
    public void specificWidthDoubleSave()
    {
        int version = 2;
        int targetWidth = 200;

        Fraction aspectRatio = new Fraction( 4, 3);

        when( cropService.getCrop( imageId, aspectRatio, targetWidth, 0 ) ).thenReturn( null );

        controller.updateCrop(imageId, aspectRatio, targetWidth, version, crop4by3, null);

        verify( cropService, times( 1 ) ).getCrop( imageId, aspectRatio, targetWidth, 0 );
        verify( cropService, times( 2 ) ).saveCrop( (Crop) anyObject());
    }

    @Test
    public void specificWidthSingleSave()
    {
        int version = 2;
        int targetWidth = 200;

        Fraction aspectRatio = new Fraction( 4, 3);

        when( cropService.getCrop( imageId, aspectRatio, targetWidth, 0 ) ).thenReturn( new Crop() );

        controller.updateCrop(imageId, aspectRatio, targetWidth, version, crop4by3, null);

        verify( cropService, times( 1 ) ).getCrop( imageId, aspectRatio, targetWidth, 0 );
        verify( cropService, times( 1 ) ).saveCrop( (Crop) anyObject());
    }

	@Test
	public void notWithinAspectRatioTolerance()
	{
		Fraction aspectRatio = new Fraction( 4, 3);

		// This assumes a sany default tolerance
		controller.updateCrop(imageId, aspectRatio, 0, 0, crop16by9, null);

		verify( cropService, times( 0 ) ).saveCrop( (Crop) anyObject());
	}

	@Test
	public void notWithinBounds()
	{
		Fraction aspectRatio = new Fraction( 16, 9);

		// This assumes a sany default tolerance
		controller.updateCrop(imageId, aspectRatio, 0, 0, crop16by9, null);

		verify( cropService, times( 0 ) ).saveCrop( (Crop) anyObject());
	}

}
