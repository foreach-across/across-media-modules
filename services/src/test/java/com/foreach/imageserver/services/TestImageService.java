package com.foreach.imageserver.services;

import com.foreach.imageserver.business.geometry.Point;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.CropDao;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.imageserver.dao.selectors.CropSelector;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import com.foreach.shared.utils.InjectUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyObject;

public class TestImageService extends AbstractServiceTest
{
    private ImageService imageService;
    private ImageDao imageDao;
	private CropDao cropDao;

    private ServableImageData testImage;
    private List<ServableImageData> testImages;
    private ImageSelector selector;

    private int testId = 1;
    private String filePath = "C:/folder/images/";
    private String originalFileName= "image";
    private String extension = "jpeg";
    private int applicationId = 20;

    @Before
    public void prepareForTest()
    {
        imageService = new ImageServiceImpl();

        imageDao = mock( ImageDao.class );
	    cropDao = mock( CropDao.class );

        InjectUtils.inject( imageService, "imageDao", imageDao);
	    InjectUtils.inject( imageService, "cropDao", cropDao);

        testImage = new ServableImageData();
        testImage.setId(testId);
        testImage.setPath(filePath);
        testImage.setOriginalFileName(originalFileName);
        testImage.setExtension(extension);

        when( imageDao.getImageById( testId ) ).thenReturn(testImage);

        selector = new ImageSelector();

        selector.setExtension(extension);
        selector.setOriginalFileName(originalFileName);
        selector.setPath(filePath);

        when( imageDao.getImageByPath( selector ) ).thenReturn(testImage);

        testImages = new ArrayList<ServableImageData>();
        testImages.add(testImage);

        when( imageDao.getAllImages() ).thenReturn( testImages );
        when( imageDao.getImageCount( (ImageSelector) anyObject() ) ).thenReturn(testImages.size());
        when( imageDao.getImages( (ImageSelector) anyObject() ) ).thenReturn(testImages);
    }

    @Test
    public void testGetImageById()
    {
        ServableImageData image = imageService.getImageById( testId );

        verify( imageDao, times( 1 ) ).getImageById(testId);

        assertNotNull( image );
        assertEquals( testId, image.getId() );
        assertEquals( extension, image.getExtension() );
        assertEquals( originalFileName, image.getOriginalFileName() );
        assertEquals( filePath, image.getPath() );
    }

    @Test
    public void testGetImagesByPath()
    {
        ServableImageData image = imageService.getImageByPath(selector);

        verify( imageDao, times( 1 ) ).getImageByPath(selector);

        assertNotNull( image );
        assertEquals( testId, image.getId() );
        assertEquals( extension, image.getExtension() );
        assertEquals( originalFileName, image.getOriginalFileName() );
        assertEquals( filePath, image.getPath() );
    }

    @Test
    public void testGetImages()
    {
        ImageSelector selector = ImageSelector.onNothing();

        List<ServableImageData> images = imageService.getImages(selector);

        verify( imageDao, times( 1 ) ).getImages(selector);

        assertNotNull( images );
        assertTrue( images.size() > 0 );
        assertEquals( testImages.size(), images.size() );
    }

    @Test
    public void testGetAllImages()
    {
        List<ServableImageData> images = imageService.getAllImages();

        verify( imageDao, times( 1 ) ).getAllImages();

        assertNotNull( images );
        assertTrue( images.size() > 0 );
        assertEquals( testImages.size(), images.size() );
    }

	@Test( expected=NullPointerException.class )
	public void testSaveNullImage()
	{
	    imageService.saveImage( null );
	}

    @Test
    public void testGetImageCount()
    {
        ImageSelector selector = ImageSelector.onApplicationId(applicationId);

        int count = imageService.getImageCount(selector);

        verify( imageDao, times( 1 ) ).getImageCount(selector);

        assertTrue( count > 0 );
        assertEquals( count, testImages.size() );
    }

    @Test
    public void testSaveNewImage()
    {
        ServableImageData newImage = new ServableImageData();

        newImage.setGroupId( 1 );
        newImage.setApplicationId( 1 );

        imageService.saveImage( newImage );

        verify( imageDao, times( 1 ) ).insertImage( newImage );
        verify(imageDao, never()).updateImage( (ServableImageData) any() );
    }

    @Test
    public void testSaveExistingImage()
    {
        ServableImageData existingImage = new ServableImageData();

        existingImage.setId( 3546 );
        existingImage.setGroupId( 1 );
        existingImage.setApplicationId( 1 );

        imageService.saveImage( existingImage );

        verify( imageDao, times( 1 ) ).updateImage( existingImage );
        verify( imageDao, never() ).insertImage( (ServableImageData) any() );
    }



	@Test
	public void testCropDeletion()
	{
	    int imageId = 3456;

		ServableImageData existingImage = new ServableImageData();

	    existingImage.setId( imageId );
	    existingImage.setGroupId( 1 );
	    existingImage.setApplicationId( 1 );
		existingImage.setWidth( 400 );
		existingImage.setHeight( 300 );

		int crop1Id = 1001;
		int crop2Id = 2002;

		List<Crop> crops = new ArrayList<Crop>();
		Crop crop1 = new Crop();
		crop1.setImageId(  imageId );
		crop1.setId( crop1Id );
		crop1.setCropRect( new Rect( new Point( 0, 0), new Size( 20, 20) ) );
		crops.add( crop1 );
		Crop crop2 = new Crop();
		crop2.setImageId( imageId );
		crop2.setId( crop2Id );
		crop2.setCropRect( new Rect( new Point( 0, 0 ), new Size( 401, 300 ) ) );
		crops.add( crop2 );

		when( cropDao.getCrops( (CropSelector) anyObject()) ).thenReturn( crops );

	    imageService.saveImage( existingImage, true );

	    verify( imageDao, times( 1 ) ).updateImage( existingImage );
	    verify( imageDao, never() ).insertImage( (ServableImageData) any() );

		verify( cropDao, times( 1 )).deleteCrop( crop2Id );
	}

}
