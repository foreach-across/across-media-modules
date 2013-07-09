package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.business.image.Dimensions;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import com.foreach.test.MockedLoader;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { TestImageService.TestConfig.class }, loader = MockedLoader.class)
public class TestImageService
{
	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageStoreService imageStoreService;

	@Autowired
	private ImageDao imageDao;

	@Test
	public void getImageByKey() {
		Image expected = new Image();
		when( imageDao.getImageByKey( "key", 15 ) ).thenReturn( expected );

		Image image = imageService.getImageByKey( "key", 15 );
		assertSame( expected, image );
	}

	@Test
	public void saveNewImage() {
		Image newImage = new Image();
		newImage.setApplicationId( 5 );
		newImage.setKey( RandomStringUtils.random( 50 ) );

		InputStream stream = mock( InputStream.class );

		RepositoryLookupResult lookupResult = new RepositoryLookupResult();
		lookupResult.setImageType( ImageType.PNG );
		lookupResult.setDimensions( new Dimensions( 1024, 768 ) );
		lookupResult.setContent( stream );

		String expectedPath = RandomStringUtils.randomAlphanumeric( 20 );
		when( imageStoreService.generateRelativeImagePath( newImage ) ).thenReturn( expectedPath );
		when( imageStoreService.saveImage( newImage, stream ) ).thenReturn( 5678L );

		imageService.save( newImage, lookupResult );

		verify( imageDao, times( 1 ) ).insertImage( newImage );
		verify( imageDao, times( 1 ) ).updateImage( newImage );
		verify( imageStoreService, never() ).deleteVariants( any( Image.class ) );

		assertEquals( expectedPath, newImage.getFilePath() );
		assertEquals( 5678, newImage.getFileSize() );
		assertEquals( ImageType.PNG, newImage.getImageType() );
		assertEquals( lookupResult.getDimensions(), newImage.getDimensions() );
	}

	@Test
	public void updateExistingImage() {
		String existingPath = RandomStringUtils.randomAlphanumeric( 20 );

		Image existing = new Image();
		existing.setId( 125 );
		existing.setApplicationId( 5 );
		existing.setKey( RandomStringUtils.random( 50 ) );
		existing.setFileSize( 1000 );
		existing.setFilePath( existingPath );
		existing.setDimensions( new Dimensions( 1600, 1200 ) );
		existing.setImageType( ImageType.JPEG );

		InputStream stream = mock( InputStream.class );

		RepositoryLookupResult lookupResult = new RepositoryLookupResult();
		lookupResult.setImageType( ImageType.PNG );
		lookupResult.setDimensions( new Dimensions( 1024, 768 ) );
		lookupResult.setContent( stream );

		when( imageStoreService.saveImage( existing, stream ) ).thenReturn( 5678L );

		imageService.save( existing, lookupResult );

		verify( imageStoreService, never() ).generateRelativeImagePath( any( Image.class ) );
		verify( imageStoreService, times( 1 ) ).deleteVariants( existing );
		verify( imageDao, times( 1 ) ).updateImage( existing );
		verify( imageDao, never() ).insertImage( any( Image.class ) );

		assertEquals( existingPath, existing.getFilePath() );
		assertEquals( 5678, existing.getFileSize() );
		assertEquals( ImageType.PNG, existing.getImageType() );
		assertEquals( lookupResult.getDimensions(), existing.getDimensions() );
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageService imageService() {
			return new ImageServiceImpl();
		}
	}

	/*

	private CropDao cropDao;

	private ServableImageData testImage;
	private List<ServableImageData> testImages;
	private ImageSelector selector;

	private int testId = 1;
	private String filePath = "C:/folder/images/";
	private String originalFileName = "image";
	private String extension = "jpeg";
	private int applicationId = 20;

	@Before
	public void prepareForTest() {
		imageService = new ImageServiceImpl();

		imageDao = mock( ImageDao.class );
		cropDao = mock( CropDao.class );

		InjectUtils.inject( imageService, "imageDao", imageDao );
		InjectUtils.inject( imageService, "cropDao", cropDao );

		testImage = new ServableImageData();
		testImage.setId( testId );
		testImage.setPath( filePath );
		testImage.setOriginalFileName( originalFileName );
		testImage.setExtension( extension );

		when( imageDao.getImageById( testId ) ).thenReturn( testImage );

		selector = new ImageSelector();

		selector.setExtension( extension );
		selector.setOriginalFileName( originalFileName );
		selector.setPath( filePath );

		when( imageDao.getImageByPath( selector ) ).thenReturn( testImage );

		testImages = new ArrayList<ServableImageData>();
		testImages.add( testImage );

		when( imageDao.getAllImages() ).thenReturn( testImages );
		when( imageDao.getImageCount( (ImageSelector) anyObject() ) ).thenReturn( testImages.size() );
		when( imageDao.getImages( (ImageSelector) anyObject() ) ).thenReturn( testImages );
	}

	@Test
	public void testGetImageById() {
		ServableImageData image = imageService.getImageById( testId );

		verify( imageDao, times( 1 ) ).getImageById( testId );

		assertNotNull( image );
		assertEquals( testId, image.getId() );
		assertEquals( extension, image.getExtension() );
		assertEquals( originalFileName, image.getOriginalFileName() );
		assertEquals( filePath, image.getPath() );
	}

	@Test
	public void testGetImagesByPath() {
		ServableImageData image = imageService.getImageByPath( selector );

		verify( imageDao, times( 1 ) ).getImageByPath( selector );

		assertNotNull( image );
		assertEquals( testId, image.getId() );
		assertEquals( extension, image.getExtension() );
		assertEquals( originalFileName, image.getOriginalFileName() );
		assertEquals( filePath, image.getPath() );
	}

	@Test
	public void testGetImages() {
		ImageSelector selector = ImageSelector.onNothing();

		List<ServableImageData> images = imageService.getImages( selector );

		verify( imageDao, times( 1 ) ).getImages( selector );

		assertNotNull( images );
		assertTrue( images.size() > 0 );
		assertEquals( testImages.size(), images.size() );
	}

	@Test
	public void testGetAllImages() {
		List<ServableImageData> images = imageService.getAllImages();

		verify( imageDao, times( 1 ) ).getAllImages();

		assertNotNull( images );
		assertTrue( images.size() > 0 );
		assertEquals( testImages.size(), images.size() );
	}

	@Test(expected = NullPointerException.class)
	public void testSaveNullImage() {
		imageService.saveImage( null );
	}

	@Test
	public void testGetImageCount() {
		ImageSelector selector = ImageSelector.onApplicationId( applicationId );

		int count = imageService.getImageCount( selector );

		verify( imageDao, times( 1 ) ).getImageCount( selector );

		assertTrue( count > 0 );
		assertEquals( count, testImages.size() );
	}

	@Test
	public void testSaveNewImage() {
		ServableImageData newImage = new ServableImageData();

		newImage.setGroupId( 1 );
		newImage.setApplicationId( 1 );

		imageService.saveImage( newImage );

		verify( imageDao, times( 1 ) ).insertImage( newImage );
		verify( imageDao, never() ).updateImage( (ServableImageData) any() );
	}

	@Test
	public void testSaveExistingImage() {
		ServableImageData existingImage = new ServableImageData();

		existingImage.setId( 3546 );
		existingImage.setGroupId( 1 );
		existingImage.setApplicationId( 1 );

		imageService.saveImage( existingImage );

		verify( imageDao, times( 1 ) ).updateImage( existingImage );
		verify( imageDao, never() ).insertImage( (ServableImageData) any() );
	}

	@Test
	public void testCropDeletion() {
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
		crop1.setImageId( imageId );
		crop1.setId( crop1Id );
		crop1.setCropRect( new Rect( new Point( 0, 0 ), new Size( 20, 20 ) ) );
		crops.add( crop1 );
		Crop crop2 = new Crop();
		crop2.setImageId( imageId );
		crop2.setId( crop2Id );
		crop2.setCropRect( new Rect( new Point( 0, 0 ), new Size( 401, 300 ) ) );
		crops.add( crop2 );

		when( cropDao.getCrops( (CropSelector) anyObject() ) ).thenReturn( crops );

		imageService.saveImage( existingImage, true );

		verify( imageDao, times( 1 ) ).updateImage( existingImage );
		verify( imageDao, never() ).insertImage( (ServableImageData) any() );

		verify( cropDao, times( 1 ) ).deleteCrop( crop2Id );
	}

	*/

}
