package com.foreach.imageserver.it;

import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.RemoteImageServerClient;
import com.foreach.imageserver.dto.*;
import org.apache.commons.io.IOUtils;
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
		String url = "http://localhost:" + System.getProperty( "local.tomcat.port", "8078" ) + "/resources/images";
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
		assertEquals( modificationDto, modifications.iterator().next() );
	}

	@Test
	public void listResolutions() {
		List<ImageResolutionDto> resolutions = imageServerClient.listAllowedResolutions( "website" );
		assertEquals( 2, resolutions.size() );
		assertTrue( hasResolution( resolutions, 640, 480, false ) );
		assertFalse( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listAllowedResolutions( "tablet" );
		assertEquals( 2, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listAllowedResolutions( null );
		assertEquals( 4, resolutions.size() );
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
		assertEquals( 2, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listConfigurableResolutions( null );
		assertEquals( 2, resolutions.size() );
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

	/*
	@Test
	public void imageUrl() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000&imageType=TIFF", url );

		url = imageServerClient.imageUrl( "10", "DIGITAL", 0, 2000, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=DIGITAL&height=2000&imageType=TIFF", url );

		url = imageServerClient.imageUrl( "someid", "SITE", 1000, 0, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=someid&context=SITE&width=1000&imageType=TIFF", url );
	}

	@Test
	public void listAllowedResolutions() {
		List<ImageResolutionDto> resolutions = imageServerClient.listAllowedResolutions( "online" );
		assertEquals( 28, resolutions.size() );

		assertEquals( 1000, resolutions.get( 0 ).getWidth() );
		assertEquals( 1000, resolutions.get( 0 ).getHeight() );

		assertEquals( 1000, resolutions.get( 1 ).getWidth() );
		assertNull( resolutions.get( 1 ).getHeight() );

		assertNull( resolutions.get( 2 ).getWidth() );
		assertEquals( 1000, resolutions.get( 2 ).getHeight() );
	}*/
}
