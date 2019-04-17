package com.foreach.imageserver.it;

import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.ImageServerException;
import com.foreach.imageserver.client.RemoteImageServerClient;
import com.foreach.imageserver.dto.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class ITRemoteImageServerClient
{
	private ImageServerClient imageServerClient;

	@Before
	public void createClient() {
		String url = "http://localhost:" + StringUtils.defaultIfEmpty( System.getProperty( "local.tomcat.port" ),
		                                                               "8078" ) + "/resources/images";
		String accessToken = "standalone-access-token";

		imageServerClient = new RemoteImageServerClient( url, accessToken );
	}

	@Test
	public void uploadKnownResourceImage() throws ParseException, IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData =
				IOUtils.toByteArray( getClass().getClassLoader().getResourceAsStream( "poppy_flower_nature.jpg" ) );
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

		inputStream = imageServerClient.imageStream( externalId, "website", 640, 480, ImageTypeDto.PNG );
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
				IOUtils.toByteArray( getClass().getClassLoader().getResourceAsStream( "poppy_flower_nature.jpg" ) );
		byte[] imageTwo =
				IOUtils.toByteArray( getClass().getClassLoader().getResourceAsStream( "transparentPngToPng.png" ) );

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
		catch ( ImageServerException ise ) {
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
	public void registerModifications() throws ParseException, IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData =
				IOUtils.toByteArray( getClass().getClassLoader().getResourceAsStream( "poppy_flower_nature.jpg" ) );

		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
		assertTrue( uploaded.isExisting() );

		Collection<ImageModificationDto> modifications = imageServerClient.listModifications( externalId, "website" );
		assertTrue( modifications.isEmpty() );

		ImageModificationDto modificationDto = new ImageModificationDto( 640, 480 );
		modificationDto.setCrop( new CropDto( 10, 10, 400, 300 ) );
		modificationDto.setDensity( new DimensionsDto( 300, 300 ) );

		imageServerClient.registerImageModification( externalId, "website", modificationDto );

		modifications = imageServerClient.listModifications( externalId, "website" );
		assertEquals( 1, modifications.size() );

		ImageModificationDto dtoWithSource = new ImageModificationDto( modificationDto );
		dtoWithSource.getCrop().setSource( new DimensionsDto( 1920, 1080 ) );

		assertEquals( dtoWithSource, modifications.iterator().next() );

		assertEquals( dtoWithSource, modifications.iterator().next() );
	}

	@Test
	public void listResolutions() {
		List<ImageResolutionDto> resolutions = imageServerClient.listAllowedResolutions( "website" );
		assertEquals( 4, resolutions.size() );
		assertTrue( hasResolution( resolutions, 640, 480, false ) );
		assertFalse( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listAllowedResolutions( "tablet" );
		assertEquals( 4, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listAllowedResolutions( null );
		assertEquals( 8, resolutions.size() );
		assertTrue( hasResolution( resolutions, 0, 0, false ) );
		assertTrue( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listConfigurableResolutions( "website" );
		assertEquals( 1, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertFalse( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listConfigurableResolutions( "tablet" );
		assertEquals( 4, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listConfigurableResolutions( null );
		assertEquals( 4, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );
	}

	private boolean hasResolution( List<ImageResolutionDto> list, int width, int height, boolean configurable ) {
		for ( ImageResolutionDto resolution : list ) {
			if ( resolution.getWidth() == width && resolution.getHeight() == height && resolution.isConfigurable() == configurable ) {
				return true;
			}
		}

		return false;
	}

	@Test
	public void pregenerateVariants() throws IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData =
				IOUtils.toByteArray( getClass().getClassLoader().getResourceAsStream( "poppy_flower_nature.jpg" ) );

		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
		assertTrue( uploaded.isExisting() );

		List<ImageResolutionDto> resolutions = imageServerClient.pregenerateResolutions( externalId );
		assertEquals( 7, resolutions.size() );
		assertTrue( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		assertTrue( consumeImage( externalId, "website", 640, 480 ) );
		assertTrue( consumeImage( externalId, "tablet", 800, 600 ) );
		assertTrue( consumeImage( externalId, "website", 1024, 768 ) );
	}

	private boolean consumeImage( String imageId, String context, int width, int height ) throws IOException {
		InputStream is = imageServerClient.imageStream( imageId, context, width, height, ImageTypeDto.JPEG );
		byte[] data = IOUtils.toByteArray( is );
		return data.length > 0;
	}

	@Test
	public void renderProvidedImage() throws IOException {
		byte[] imageData =
				IOUtils.toByteArray(
						getClass().getClassLoader().getResourceAsStream( "poppy_flower_nature.jpg" ) );

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
						getClass().getClassLoader().getResourceAsStream( "poppy_flower_nature.jpg" ) );

		ImageInfoDto imageInfoDto = imageServerClient.imageInfo( imageData );
		assertEquals( new DimensionsDto( 1920, 1080 ), imageInfoDto.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, imageInfoDto.getImageType() );
	}
}
