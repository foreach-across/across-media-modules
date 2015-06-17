package com.foreach.imageserver.core.managers;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.imageserver.core.AbstractCachedIntegrationTest;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.repositories.ImageContextRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.Assert.*;

public class ImageContextManagerTest extends AbstractCachedIntegrationTest
{
	@Autowired
	private ImageContextManager contextManager;

	@Autowired
	private ImageContextRepository imageContextRepository;

	@Autowired
	private CacheManager cacheManager;

	@Test
	public void getByCode() {
		ImageContext context = new ImageContext();
		context.setId( 10 );
		context.setCode( "the_application_code" );

		Cache cache = cacheManager.getCache( "contexts" );
		assertNotNull( cache );
		assertNull( cache.get( "byCode-the_application_code" ) );

		imageContextRepository.create( context );

		ImageContext retrievedContext = contextManager.getByCode( "the_application_code" );
		assertNotNull( retrievedContext );
		assertEquals( 10, retrievedContext.getId() );
		assertEquals( "the_application_code", retrievedContext.getCode() );
		assertSame( retrievedContext, cache.get( "byCode-the_application_code" ).get() );

		// Reuse the initial object instead of changing the pointers to the objects in cache
		context.setCode( "the_other_application_code" );
		imageContextRepository.update( context );

		ImageContext retrievedAgainContext = contextManager.getByCode( "the_application_code" );
		assertSame( retrievedContext, retrievedAgainContext );
		assertEquals( 10, retrievedAgainContext.getId() );
		assertEquals( "the_application_code", retrievedAgainContext.getCode() );
	}
}
