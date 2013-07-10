package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageType;
import com.foreach.shared.utils.DateUtils;
import com.foreach.test.MockedLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { TestImageStoreService.TestConfig.class }, loader = MockedLoader.class)
public class TestImageStoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( TestImageStoreService.class );

	private static String TEMP_DIR;
	private static String ORIGINAL_STORE;
	private static String VARIANT_STORE;

	@Autowired
	private ImageStoreService imageStoreService;

	@BeforeClass
	public static void createStorePaths() {
		TEMP_DIR =
				System.getProperty( "java.io.tmpdir" ) + "/imgsrv-" + RandomStringUtils.randomAlphanumeric( 15 ) + "/";

		assertFalse( "Temp image store directory should not yet exist", new File( TEMP_DIR ).exists() );
		ORIGINAL_STORE = TEMP_DIR + "original";
		VARIANT_STORE = TEMP_DIR + "/variant";
	}

	@AfterClass
	public static void cleanupStorePaths() {
		try {
			FileUtils.deleteDirectory( new File( TEMP_DIR ) );
		}
		catch ( IOException ioe ) {
			LOG.warn( "Could not cleanup unit test path: ", ioe );
		}
	}

	@Test
	public void generateRelativeImagePath() {
		Image image = new Image();
		image.setDateCreated( DateUtils.parseDate( "2013-07-06 13:35:13" ) );

		String path = imageStoreService.generateRelativeImagePath( image );

		assertEquals( "/2013/07/06/", path );
	}

	@Test
	public void saveNewImage() throws Exception {
		Image image = new Image();
		image.setId( 2 );
		image.setApplicationId( 10 );
		image.setFilePath( "/2013/07/09/" );
		image.setImageType( ImageType.JPEG );

		saveAndVerify( image, ImageTestData.EARTH, ORIGINAL_STORE, "/10/2013/07/09/2.jpeg" );
	}

	@Test
	public void updateImage() throws Throwable {
		Image image = new Image();
		image.setId( 3 );
		image.setApplicationId( 10 );
		image.setFilePath( "/2013/07/09/" );
		image.setImageType( ImageType.JPEG );

		saveAndVerify( image, ImageTestData.EARTH, ORIGINAL_STORE, "/10/2013/07/09/3.jpeg" );
		saveAndVerify( image, ImageTestData.SUNSET, ORIGINAL_STORE, "/10/2013/07/09/3.jpeg" );
	}

	private void saveAndVerify( Image image,
	                            ImageTestData testData,
	                            String path,
	                            String expectedFileName ) throws Exception {
		InputStream imageData = testData.getResourceAsStream();

		File expectedFile = new File( path, expectedFileName );
		long fileSize = imageStoreService.saveImage( image, imageData );

		assertEquals( testData.getFileSize(), fileSize );
		assertTrue( expectedFile.exists() );
		FileInputStream fos = new FileInputStream( expectedFile );
		assertTrue( IOUtils.contentEquals( testData.getResourceAsStream(), fos ) );
		fos.close();
	}

	@Test(expected = ImageStoreOperationException.class)
	public void failSavingOriginal() throws Exception {
		Image image = new Image();
		image.setId( 2 );
		image.setApplicationId( 10 );
		image.setFilePath( "/2013/07/09/" );
		image.setImageType( ImageType.JPEG );

		imageStoreService.saveImage( image, null );
	}

	@Test
	public void deleteVariants() throws Exception {
		Image image = new Image();
		image.setId( 2 );
		image.setApplicationId( 10 );
		image.setFilePath( "/2013/07/06/" );
		image.setImageType( ImageType.JPEG );

		// Put dummy files in place
		new File( VARIANT_STORE, "/10/2013/07/06/" ).mkdirs();
		new File( ORIGINAL_STORE, "/10/2013/07/06/" ).mkdirs();

		File original = createDummy( ORIGINAL_STORE, "/10/2013/07/06/2.jpeg" );
		File variantOne = createDummy( VARIANT_STORE, "/10/2013/07/06/2.100x200.jpeg" );
		File variantTwo = createDummy( VARIANT_STORE, "/10/2013/07/06/2.test.png" );
		File otherVariant = createDummy( VARIANT_STORE, "/10/2013/07/06/3.100x200.jpeg" );

		assertTrue( original.exists() );
		assertTrue( variantOne.exists() );
		assertTrue( variantTwo.exists() );
		assertTrue( otherVariant.exists() );

		imageStoreService.deleteVariants( image );

		assertTrue( original.exists() );
		assertFalse( variantOne.exists() );
		assertFalse( variantTwo.exists() );
		assertTrue( otherVariant.exists() );
	}

	private File createDummy( String path, String fileName ) throws Exception {
		File file = new File( path, fileName );
		FileOutputStream fos = new FileOutputStream( file );
		IOUtils.write( new byte[0], fos );
		fos.close();

		return file;
	}

	@Test
	public void getOriginalImageFile() throws Exception {
		Image image = new Image();
		image.setId( 3 );
		image.setApplicationId( 10 );
		image.setFilePath( "/2013/07/06/" );
		image.setImageType( ImageType.JPEG );

		new File( ORIGINAL_STORE, "/10/2013/07/06/" ).mkdirs();

		File actual = createActual( ORIGINAL_STORE, "/10/2013/07/06/3.jpeg", ImageTestData.SUNSET );

		ImageFile imageFile = imageStoreService.getImageFile( image );

		assertNotNull( imageFile );
		assertEquals( ImageType.JPEG, imageFile.getImageType() );
		assertEquals( ImageTestData.SUNSET.getFileSize(), imageFile.getFileSize() );
		FileInputStream fos = new FileInputStream( actual );
		assertTrue( IOUtils.contentEquals( ImageTestData.SUNSET.getResourceAsStream(), fos ) );
		fos.close();
		imageFile.getContent().close();
	}

	private File createActual( String path, String fileName, ImageTestData testData ) throws Exception {
		File file = new File( path, fileName );
		FileOutputStream fos = new FileOutputStream( file );
		IOUtils.copy( testData.getResourceAsStream(), fos );
		fos.close();

		return file;
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageStoreService imageStoreService() {
			return new ImageStoreServiceImpl( ORIGINAL_STORE, VARIANT_STORE );
		}
	}
}
