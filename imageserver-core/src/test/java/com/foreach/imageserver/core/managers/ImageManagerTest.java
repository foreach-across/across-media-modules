package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.AbstractCachedIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.repositories.ImageRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;

public class ImageManagerTest extends AbstractCachedIntegrationTest
{

	@Autowired
	private ImageManager imageManager;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private ImageRepository imageRepository;

	@Test
	public void insertGetById() {
		Image insertedImage = image( "externalId", new Date(), 100, 200, ImageType.GIF );
		imageManager.insert( insertedImage );

		Cache cache = cacheManager.getCache( "images" );
		assertNotNull( cache );
		assertNull( cache.get( "byId-" + insertedImage.getId() ) );

		Image retrievedImage = imageManager.getById( insertedImage.getId() );
		shouldBeEqual( insertedImage, retrievedImage );
		assertSame( retrievedImage, cache.get( "byId-" + insertedImage.getId() ).get() );

		deleteAllImages();

		Image retrievedAgainImage = imageManager.getById( insertedImage.getId() );
		shouldBeEqual( insertedImage, retrievedAgainImage );
		assertSame( retrievedImage, retrievedAgainImage );
	}

	private void deleteAllImages() {
		Collection<Image> images = imageRepository.getAll();
		for( Image image : images ) {
			imageRepository.delete( image );
		}
	}

	@Test
	public void insertGetByExternalId() {
		// Things will go wrong if this null result is cached.
		assertNull( imageManager.getByExternalId( "externalId" ) );

		Image insertedImage = image( "externalId", new Date(), 100, 200, ImageType.GIF );
		imageManager.insert( insertedImage );

		Cache cache = cacheManager.getCache( "images" );
		assertNotNull( cache );
		assertNull( cache.get( "byExternalId-" + insertedImage.getExternalId() ) );

		Image retrievedImage = imageManager.getByExternalId( insertedImage.getExternalId() );
		shouldBeEqual( insertedImage, retrievedImage );
		assertSame( retrievedImage, cache.get( "byExternalId-" + insertedImage.getExternalId() ).get() );

		deleteAllImages();

		Image retrievedAgainImage = imageManager.getByExternalId( insertedImage.getExternalId() );
		shouldBeEqual( insertedImage, retrievedAgainImage );
		assertSame( retrievedImage, retrievedAgainImage );
	}

	private Image image( String externalId, Date date, int width, int height, ImageType imageType ) {
		Image image = new Image();
		image.setExternalId( externalId );
		image.setDateCreated( date );
		image.setDimensions( new Dimensions( width, height ) );
		image.setImageType( imageType );
		image.setImageProfileId( ImageProfile.DEFAULT_PROFILE_ID );
		return image;
	}

	private void shouldBeEqual( Image lhsImage, Image rhsImage ) {
		assertEquals( lhsImage.getId(), rhsImage.getId() );
		assertEquals( lhsImage.getExternalId(), rhsImage.getExternalId() );
		//TODO: use DateUtils.truncatedEquals()
		assertEquals( lhsImage.getDateCreated(), rhsImage.getDateCreated() );
		assertEquals( lhsImage.getDimensions().getWidth(), rhsImage.getDimensions().getWidth() );
		assertEquals( lhsImage.getDimensions().getHeight(), rhsImage.getDimensions().getHeight() );
		assertEquals( lhsImage.getImageType(), rhsImage.getImageType() );
	}

}
