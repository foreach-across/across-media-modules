package com.foreach.imageserver.services;

import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.services.exceptions.TempStoreOperationException;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { TestTempFileService.TestConfig.class }, loader = MockedLoader.class)
public class TestTempFileService
{
	private static final Logger LOG = LoggerFactory.getLogger( TestTempFileService.class );

	private static String TEMP_DIR;

	@Autowired
	private TempFileService tempFileService;

	@BeforeClass
	public static void createStorePaths() {
		TEMP_DIR = System.getProperty( "java.io.tmpdir" ) + "/imgsrv-" + RandomStringUtils.randomAlphanumeric(
				15 ) + "/tmp";

		assertFalse( "Temp store directory should not yet exist", new File( TEMP_DIR ).exists() );
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
	public void regularImageFileShouldNotBeATempFile() {
		assertFalse( tempFileService.isTempFile( new ImageFile( ImageType.PNG, 0, null ) ) );
	}

	@Test
	public void imageFileCreatedByTempFileServiceShouldBeATempFileWithSameContent() throws IOException {
		ImageFile tempFile =
				tempFileService.createImageFile( ImageType.JPEG, ImageTestData.EARTH.getResourceAsStream() );

		assertTrue( tempFileService.isTempFile( tempFile ) );
		assertEquals( ImageType.JPEG, tempFile.getImageType() );
		assertEquals( ImageTestData.EARTH.getFileSize(), tempFile.getFileSize() );
		InputStream tempStream = tempFile.openContentStream();
		IOUtils.contentEquals( ImageTestData.EARTH.getResourceAsStream(), tempStream );
		IOUtils.closeQuietly( tempStream );
	}

	@Test(expected = TempStoreOperationException.class)
	public void moveOnNonTempFileShouldFail() {
		File destination = new File( TEMP_DIR, "destination.jpeg" );
		tempFileService.move( new ImageFile( ImageType.JPEG, 0, null ), destination );
	}

	@Test
	public void moveOnTempFileShouldFirstTryRename() {
		File destination = new File( TEMP_DIR, "destination2.jpeg" );
		assertFalse( destination.exists() );

		TempFileServiceImpl.TempImageFile tempFile = mock( TempFileServiceImpl.TempImageFile.class );
		assertTrue( "Mock should validate as temporary ImageFile", tempFileService.isTempFile( tempFile ) );

		File mockPhysical = mock( File.class );
		when( tempFile.getPhysicalFile() ).thenReturn( mockPhysical );
		when( mockPhysical.renameTo( destination ) ).thenReturn( true );

		ImageFile newPhysical = tempFileService.move( tempFile, destination );

		verify( mockPhysical, times( 1 ) ).renameTo( destination );

		assertFalse( destination.exists() );
		assertNotNull( newPhysical );
	}

	@Test(expected = NullPointerException.class)
	public void moveOnFakeFileShouldFailOnCopyIfRenameFailed() {
		File destination = new File( TEMP_DIR, "destination3.jpeg" );
		assertFalse( destination.exists() );

		TempFileServiceImpl.TempImageFile tempFile = mock( TempFileServiceImpl.TempImageFile.class );
		assertTrue( "Mock should validate as temporary ImageFile", tempFileService.isTempFile( tempFile ) );

		File mockPhysical = mock( File.class );
		when( tempFile.getPhysicalFile() ).thenReturn( mockPhysical );
		when( mockPhysical.renameTo( destination ) ).thenReturn( false );

		try {
			tempFileService.move( tempFile, destination );
		}
		finally {
			verify( mockPhysical, times( 1 ) ).renameTo( destination );
		}
	}

	@Test
	public void moveOnTempFileShouldAtLeaseCreateCopy() throws IOException {
		File destination = new File( TEMP_DIR, "destination4.jpeg" );
		assertFalse( destination.exists() );

		ImageFile tempFile = tempFileService.createImageFile( ImageTestData.SUNSET.getImageType(),
		                                                      ImageTestData.SUNSET.getResourceAsStream() );
		ImageFile actualFile = tempFileService.move( tempFile, destination );

		assertNotNull( actualFile );
		assertTrue( destination.exists() );
		assertFalse( tempFileService.isTempFile( actualFile ) );
		assertEquals( ImageTestData.SUNSET.getImageType(), actualFile.getImageType() );
		assertEquals( ImageTestData.SUNSET.getFileSize(), actualFile.getFileSize() );

		InputStream stream = actualFile.openContentStream();
		IOUtils.contentEquals( ImageTestData.SUNSET.getResourceAsStream(), stream );
		IOUtils.closeQuietly( stream );
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public TempFileService tempFileService() {
			return new TempFileServiceImpl( TEMP_DIR );
		}
	}

}
