package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.ImageResolution;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.Assert.*;

@Ignore( "CacheManager later" )
public class ImageResolutionManagerTest extends AbstractIntegrationTest
{

	@Autowired
	private ImageResolutionManager imageResolutionManager;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void getById() {
		jdbcTemplate.update( "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )", 10, 111, 222 );

		Cache cache = cacheManager.getCache( "imageResolutions" );
		assertNotNull( cache );
		assertNull( cache.get( "byId-10" ) );

		ImageResolution retrievedResolution = imageResolutionManager.getById( 10 );
		assertEquals( 10, retrievedResolution.getId() );
		assertEquals( 111, retrievedResolution.getWidth());
		assertEquals( 222, retrievedResolution.getHeight() );
		assertSame( retrievedResolution, cache.get( "byId-10" ).get() );

		jdbcTemplate.execute( "DELETE FROM IMAGE_RESOLUTION" );

		ImageResolution retrievedAgainResolution = imageResolutionManager.getById( 10 );
		assertSame( retrievedResolution, retrievedAgainResolution );
		assertEquals( 10, retrievedAgainResolution.getId() );
		assertEquals( 111, retrievedAgainResolution.getWidth());
		assertEquals( 222, retrievedAgainResolution.getHeight() );
	}

	@Test
	public void getForContext() {
		jdbcTemplate.update( "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )", 10, "the_application_code" );
		jdbcTemplate.update( "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )", 11, 222, 333 );
		jdbcTemplate.update( "INSERT INTO CONTEXT_IMAGE_RESOLUTION ( contextId, imageResolutionId ) VALUES ( ?, ? )",
		                     10, 11 );

		Cache cache = cacheManager.getCache( "imageResolutions" );
		assertNotNull( cache );
		assertNull( cache.get( "forContext-10" ) );

		List<ImageResolution> retrievedList = imageResolutionManager.getForContext( 10 );
		assertEquals( 1, retrievedList.size() );
		assertEquals( 11, retrievedList.get( 0 ).getId() );
		assertSame( retrievedList, cache.get( "forContext-10" ).get() );

		jdbcTemplate.execute( "DELETE FROM CONTEXT_IMAGE_RESOLUTION" );
		jdbcTemplate.execute( "DELETE FROM IMAGE_RESOLUTION" );
		jdbcTemplate.execute( "DELETE FROM CONTEXT" );

		List<ImageResolution> retrievedAgainList = imageResolutionManager.getForContext( 10 );
		assertSame( retrievedList, retrievedAgainList );
		assertEquals( 1, retrievedAgainList.size() );
		assertEquals( 11, retrievedAgainList.get( 0 ).getId() );
	}

}
