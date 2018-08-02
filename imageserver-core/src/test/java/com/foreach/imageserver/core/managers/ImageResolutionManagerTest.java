package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.AbstractCachedIntegrationTest;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.repositories.ImageContextRepository;
import com.foreach.imageserver.core.repositories.ImageResolutionRepository;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class ImageResolutionManagerTest extends AbstractCachedIntegrationTest
{
	@Autowired
	private ImageResolutionManager imageResolutionManager;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private ImageResolutionRepository imageResolutionRepository;

	@Autowired
	private ImageContextRepository imageContextRepository;

	@Test
	public void getById() {
		createImageResolution( 10, 111, 222, null );

		Cache cache = cacheManager.getCache( "imageResolutions" );
		assertNotNull( cache );
		assertNull( cache.get( "byId-10" ) );

		ImageResolution retrievedResolution = imageResolutionManager.getById( 10 );
		assertEquals( 10, retrievedResolution.getId() );
		assertEquals( 111, retrievedResolution.getWidth() );
		assertEquals( 222, retrievedResolution.getHeight() );
		assertSame( retrievedResolution, cache.get( "byId-10" ).get() );

		deleteImageResolutions();

		ImageResolution retrievedAgainResolution = imageResolutionManager.getById( 10 );
		assertSame( retrievedResolution, retrievedAgainResolution );
		assertEquals( 10, retrievedAgainResolution.getId() );
		assertEquals( 111, retrievedAgainResolution.getWidth() );
		assertEquals( 222, retrievedAgainResolution.getHeight() );
	}

	private void createImageResolution( long id, int width, int height, Collection<ImageContext> contexts ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( width );
		imageResolution.setHeight( height );
		if( contexts != null ) {
			imageResolution.setContexts( contexts );
		}
		imageResolutionRepository.create( imageResolution );
	}

	@Test
	public void getForContext() {
		ImageContext context = new ImageContext();
		context.setId( 17L );
		context.setCode( "the_application_code_17" );
		imageContextRepository.save( context );

		createImageResolution( 11, 222, 333, Sets.newSet( context ) );

		Cache cache = cacheManager.getCache( "imageResolutions" );
		assertNotNull( cache );
		assertNull( cache.get( "forContext-10" ) );

		List<ImageResolution> retrievedList = imageResolutionManager.getForContext( 17 );
		assertEquals( 1, retrievedList.size() );
		assertEquals( 11, retrievedList.get( 0 ).getId() );
		assertSame( retrievedList, cache.get( "forContext-17" ).get() );

		deleteImageResolutions();
		deleteContexts();

		List<ImageResolution> retrievedAgainList = imageResolutionManager.getForContext( 17 );
		assertSame( retrievedList, retrievedAgainList );
		assertEquals( 1, retrievedAgainList.size() );
		assertEquals( 11, retrievedAgainList.get( 0 ).getId() );
	}

	private void deleteContexts() {
		Collection<ImageContext> imageContexts = imageContextRepository.findAll();
		for( ImageContext imageContext : imageContexts ) {
			imageContextRepository.delete( imageContext );
		}
	}

	private void deleteImageResolutions() {
		Collection<ImageResolution> imageResolutions = imageResolutionRepository.findAll();
		for( ImageResolution imageResolution : imageResolutions ) {
			imageResolutionRepository.delete( imageResolution );
		}
	}

}
