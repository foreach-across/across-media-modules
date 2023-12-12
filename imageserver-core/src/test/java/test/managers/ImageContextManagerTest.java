package test.managers;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.managers.ImageContextManager;
import com.foreach.imageserver.core.repositories.ImageContextRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import test.AbstractCachedIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

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
		context.setId( 10L );
		context.setCode( "the_application_code" );

		Cache cache = cacheManager.getCache( "contexts" );
		assertNotNull( cache );
		assertNull( cache.get( "byCode-the_application_code" ) );

		imageContextRepository.save( context );

		ImageContext retrievedContext = contextManager.getByCode( "the_application_code" );
		assertNotNull( retrievedContext );
		assertEquals( 10L, retrievedContext.getId().longValue() );
		assertEquals( "the_application_code", retrievedContext.getCode() );
		assertSame( retrievedContext, cache.get( "byCode-the_application_code" ).get() );

		// Reuse the initial object instead of changing the pointers to the objects in cache
		context.setCode( "the_other_application_code" );
		imageContextRepository.save( context );

		ImageContext retrievedAgainContext = contextManager.getByCode( "the_application_code" );
		assertSame( retrievedContext, retrievedAgainContext );
		assertEquals( 10L, retrievedAgainContext.getId().longValue() );
		assertEquals( "the_application_code", retrievedAgainContext.getCode() );
	}
}
