package com.foreach.imageserver.it.core;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.config.WebConfiguration;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
import com.foreach.imageserver.dto.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITLocalImageServerClient.Config.class)
@TestPropertySource(properties = { "spring.jpa.show-sql=true" })
public class ITLocalImageServerClient
{
	@Autowired(required = false)
	private ImageServerClient imageServerClient;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageContextService imageContextService;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void noImageRequestHashBuilderShouldBeCreated() {
		assertNull( beanRegistry.getBeanFromModule( ImageServerCoreModule.NAME,
		                                            WebConfiguration.IMAGE_REQUEST_HASH_BUILDER ) );
	}

	@Before
	public void registerResolutions() {
		registerResolution( 640, 480 );
		registerResolution( 320, 240 );
	}

	private void registerResolution( int width, int height ) {
		ImageResolution resolution = imageService.getResolution( width, height );

		if ( resolution == null ) {
			resolution = new ImageResolution();
			resolution.setWidth( width );
			resolution.setHeight( height );
			resolution.setConfigurable( true );
			resolution.setContexts( Collections.singleton( imageContextService.getByCode( "default" ) ) );
			resolution.setAllowedOutputTypes( EnumSet.allOf( ImageType.class ) );

			imageService.saveImageResolution( resolution );
		}
	}

	@Test
	public void clientShouldBeCreated() {
		assertNotNull( imageServerClient );
		assertEquals( "http://somehost/img", imageServerClient.getImageServerUrl() );
	}

	@Test
	public void uploadingKnownResourceImage() throws Exception {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "images/poppy_flower_nature.jpg" ) );
		Date date = DateUtils.parseDate( "2013-05-14 13:33:22", "yyyy-MM-dd HH:mm:ss" );

		ImageInfoDto fetchedInfo = imageServerClient.imageInfo( externalId );
		assertFalse( fetchedInfo.isExisting() );
		assertEquals( externalId, fetchedInfo.getExternalId() );

		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageData, date );

		assertTrue( createdInfo.isExisting() );
		assertEquals( externalId, createdInfo.getExternalId() );
		assertEquals( date, createdInfo.getCreated() );
		assertEquals( new DimensionsDto( 1920, 1080 ), createdInfo.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, createdInfo.getImageType() );

		fetchedInfo = imageServerClient.imageInfo( externalId );
		assertEquals( createdInfo, fetchedInfo );

		InputStream inputStream = imageServerClient.imageStream( externalId, new ImageModificationDto(),
		                                                         new ImageVariantDto( ImageTypeDto.JPEG ) );
		byte[] originalSizeData = IOUtils.toByteArray( inputStream );

		ImageInfoDto modifiedUpload = imageServerClient.loadImage( UUID.randomUUID().toString(), originalSizeData );
		assertEquals( new DimensionsDto( 1920, 1080 ), modifiedUpload.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, modifiedUpload.getImageType() );

		inputStream = imageServerClient.imageStream( externalId, "default", 640, 480, ImageTypeDto.PNG );
		byte[] scaledDate = IOUtils.toByteArray( inputStream );

		modifiedUpload = imageServerClient.loadImage( UUID.randomUUID().toString(), scaledDate );
		assertEquals( new DimensionsDto( 640, 480 ), modifiedUpload.getDimensionsDto() );
		assertEquals( ImageTypeDto.PNG, modifiedUpload.getImageType() );

		// Delete existing
		assertTrue( imageServerClient.deleteImage( externalId ) );
		assertFalse( imageServerClient.imageInfo( externalId ).isExisting() );
		assertFalse( imageServerClient.imageExists( externalId ) );

		assertFalse( imageServerClient.deleteImage( externalId ) );

		imageServerClient.loadImage( externalId, imageData, date );
		assertTrue( imageServerClient.imageExists( externalId ) );
	}

	@Test
	public void replacingImage() throws Exception {
		String externalId = UUID.randomUUID().toString();
		byte[] imageOne =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "images/poppy_flower_nature.jpg" ) );
		byte[] imageTwo =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "images/transparentPngToPng.png" ) );

		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageOne );
		assertTrue( createdInfo.isExisting() );
		assertEquals( externalId, createdInfo.getExternalId() );
		assertEquals( new DimensionsDto( 1920, 1080 ), createdInfo.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, createdInfo.getImageType() );

		ImageInfoDto replaced;
		boolean failed = false;

		try {
			imageServerClient.loadImage( externalId, imageTwo );
		}
		catch ( ImageStoreException ise ) {
			failed = true;
		}

		assertTrue( failed );

		replaced = imageServerClient.loadImage( externalId, imageTwo, true );
		assertNotNull( replaced );
		assertTrue( replaced.isExisting() );
		assertEquals( externalId, replaced.getExternalId() );
		assertEquals( new DimensionsDto( 100, 100 ), replaced.getDimensionsDto() );
		assertEquals( ImageTypeDto.PNG, replaced.getImageType() );

		ImageInfoDto fetched = imageServerClient.imageInfo( externalId );
		assertNotNull( fetched );
		assertTrue( fetched.isExisting() );
		assertEquals( externalId, fetched.getExternalId() );
		assertEquals( new DimensionsDto( 100, 100 ), fetched.getDimensionsDto() );
	}

	@Test
	public void registerModification() throws ParseException, IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "images/poppy_flower_nature.jpg" ) );

		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
		assertTrue( uploaded.isExisting() );

		Collection<ImageModificationDto> modifications = imageServerClient.listModifications( externalId, "default" );
		assertTrue( modifications.isEmpty() );

		ImageModificationDto modificationDto = new ImageModificationDto( 640, 480 );
		modificationDto.setCrop( new CropDto( 10, 10, 400, 300 ) );
		modificationDto.setDensity( new DimensionsDto( 300, 300 ) );

		imageServerClient.registerImageModification( externalId, "default", modificationDto );

		modifications = imageServerClient.listModifications( externalId, "default" );
		assertEquals( 1, modifications.size() );

		ImageModificationDto dtoWithSource = new ImageModificationDto( modificationDto );
		dtoWithSource.getCrop().setSource( new DimensionsDto( 1920, 1080 ) );

		assertEquals( dtoWithSource, modifications.iterator().next() );
	}

	@Test
	public void registerModifications() throws ParseException, IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "images/poppy_flower_nature.jpg" ) );

		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
		assertTrue( uploaded.isExisting() );

		Collection<ImageModificationDto> modifications = imageServerClient.listModifications( externalId, "default" );
		assertTrue( modifications.isEmpty() );

		ImageModificationDto modificationDto = new ImageModificationDto( 640, 480 );
		modificationDto.setCrop( new CropDto( 10, 10, 400, 300 ) );
		modificationDto.setDensity( new DimensionsDto( 300, 300 ) );

		ImageModificationDto modificationDto2 = new ImageModificationDto( 320, 240 );
		modificationDto.setCrop( new CropDto( 10, 10, 100, 150 ) );
		modificationDto.setDensity( new DimensionsDto( 100, 100 ) );

		List<ImageModificationDto> imageModificationDtos = Arrays.asList( modificationDto, modificationDto2 );

		imageServerClient.registerImageModifications( externalId, "default", imageModificationDtos );

		modifications = imageServerClient.listModifications( externalId, "default" );
		assertEquals( 2, modifications.size() );

		ImageModificationDto dtoWithSource = new ImageModificationDto( modificationDto );
		dtoWithSource.getCrop().setSource( new DimensionsDto( 1920, 1080 ) );

		assertEquals( dtoWithSource, modifications.iterator().next() );
	}

	@Test
	public void renderProvidedImage() throws IOException {
		byte[] imageData =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "images/poppy_flower_nature.jpg" ) );

		InputStream renderedImage = imageServerClient.imageStream( imageData, new ImageModificationDto( 100, 100 ),
		                                                           new ImageVariantDto( ImageTypeDto.PNG ) );

		byte[] scaledDate = IOUtils.toByteArray( renderedImage );

		ImageInfoDto modifiedUpload = imageServerClient.loadImage( UUID.randomUUID().toString(), scaledDate );
		assertEquals( new DimensionsDto( 100, 100 ), modifiedUpload.getDimensionsDto() );
		assertEquals( ImageTypeDto.PNG, modifiedUpload.getImageType() );
	}

	@Test
	public void imageInfoForGivenImage() throws IOException {
		byte[] imageData =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "images/poppy_flower_nature.jpg" ) );

		ImageInfoDto imageInfoDto = imageServerClient.imageInfo( imageData );
		assertEquals( new DimensionsDto( 1920, 1080 ), imageInfoDto.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, imageInfoDto.getImageType() );
	}

	@Configuration
	@AcrossTestConfiguration(modules = { FileManagerModule.NAME, PropertiesModule.NAME })
	@PropertySource("classpath:integrationtests.properties")
	protected static class Config implements AcrossContextConfigurer
	{

		@Value("${transformer.imagemagick.path}")
		String imageMagickPath;

		@Override
		public void configure( AcrossContext context ) {
			context.addModule( imageServerCoreModule() );
		}

		private ImageServerCoreModule imageServerCoreModule() {
			ImageServerCoreModule imageServerCoreModule = new ImageServerCoreModule();
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
			                                   System.getProperty( "java.io.tmpdir" ) );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.ROOT_PATH, "/imgsrvr" );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.PROVIDE_STACKTRACE, true );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED, true );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_USE_GRAPHICSMAGICK, true );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_PATH,
			                                   imageMagickPath );

			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.CREATE_LOCAL_CLIENT, true );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_SERVER_URL, "http://somehost/img" );

			return imageServerCoreModule;
		}
	}

}
