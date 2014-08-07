package com.foreach.imageserver.it.core;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.test.AcrossTestWebConfiguration;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITImageServerCoreWithLocalClient.Config.class)
public class ITImageServerCoreWithLocalClient
{
	@Autowired(required = false)
	private ImageServerClient imageServerClient;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageContextService imageContextService;

	@Before
	public void registerResolution() {
		ImageResolution resolution = imageService.getResolution( 640, 480 );

		if ( resolution == null ) {
			resolution = new ImageResolution();
			resolution.setWidth( 640 );
			resolution.setHeight( 480 );
			resolution.setConfigurable( true );
			resolution.setContexts( Collections.singleton( imageContextService.getByCode( "default" ) ) );

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
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( imageServerCoreModule() );
			context.addModule( new AcrossHibernateModule() );
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
			                                   "c:/Program Files/GraphicsMagick-1.3.19-Q8" );

			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.CREATE_LOCAL_CLIENT, true );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_SERVER_URL, "http://somehost/img" );

			return imageServerCoreModule;
		}
	}

}
