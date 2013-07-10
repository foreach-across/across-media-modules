package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
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
	private ImageModificationService imageModificationService;

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
		when( imageStoreService.saveImage( newImage, stream ) ).thenReturn( new ImageFile( null, null, 5678L ) );

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

		when( imageStoreService.saveImage( existing, stream ) ).thenReturn( new ImageFile( null, null, 5678L ) );

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

	@Test
	public void fetchImageFileThatExists() {
		Image image = new Image();
		ImageFile imageFile = new ImageFile( ImageType.JPEG, 0, null );
		ImageModifier modifier = new ImageModifier();

		when( imageStoreService.getImageFile( image, modifier ) ).thenReturn( imageFile );

		ImageFile returned = imageService.fetchImageFile( image, modifier );
		assertSame( imageFile, returned );

		verify( imageStoreService, never() ).getImageFile( any( Image.class ) );
		verify( imageModificationService, never() ).apply( any( ImageFile.class ), any( ImageModifier.class ) );
		verify( imageStoreService, never() ).saveImageFile( any( Image.class ), any( ImageModifier.class ),
		                                                    any( ImageFile.class ) );
	}

	@Test
	public void fetchImageFileThatDoesNotExist() {
		Image image = new Image();
		ImageModifier modifier = new ImageModifier();

		ImageFile original = new ImageFile( ImageType.GIF, 0, null );
		ImageFile renderedFile = new ImageFile( ImageType.JPEG, 0, null );
		ImageFile storedFile = new ImageFile( ImageType.PNG, 0, null );

		when( imageStoreService.getImageFile( image, modifier ) ).thenReturn( null );
		when( imageStoreService.getImageFile( image ) ).thenReturn( original );
		when( imageModificationService.apply( original, modifier ) ).thenReturn( renderedFile );
		when( imageStoreService.saveImageFile( image, modifier, renderedFile ) ).thenReturn( storedFile );

		ImageFile returned = imageService.fetchImageFile( image, modifier );

		assertSame( storedFile, returned );
	}

	@Test
	public void deleteImage() {
		Image image = new Image();
		image.setId( 123 );

		imageService.delete( image );

		verify( imageDao, times( 1 ) ).deleteImage( image.getId() );
		verify( imageStoreService, times( 1 ) ).delete( image );
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageService imageService() {
			return new ImageServiceImpl();
		}
	}
}
