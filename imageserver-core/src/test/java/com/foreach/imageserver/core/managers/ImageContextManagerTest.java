package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.ImageContext;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.*;

@Ignore("CacheManager later")
public class ImageContextManagerTest extends AbstractIntegrationTest
{

	@Autowired
	private ImageContextManager contextManager;

	@Autowired
	private CacheManager cacheManager;

	private JdbcTemplate jdbcTemplate;

	@Test
	public void getByCode() {
		String insertStatement = "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )";
		jdbcTemplate.update( insertStatement, 10, "the_application_code" );

		Cache cache = cacheManager.getCache( "contexts" );
		assertNotNull( cache );
		assertNull( cache.get( "byCode-the_application_code" ) );

		ImageContext retrievedContext = contextManager.getByCode( "the_application_code" );
		assertEquals( 10, retrievedContext.getId() );
		assertEquals( "the_application_code", retrievedContext.getCode() );
		assertSame( retrievedContext, cache.get( "byCode-the_application_code" ).get() );

		String updateStatement = "UPDATE CONTEXT SET code = ? WHERE id = ?";
		jdbcTemplate.update( updateStatement, "the_other_application_code", 10 );

		ImageContext retrievedAgainContext = contextManager.getByCode( "the_application_code" );
		assertSame( retrievedContext, retrievedAgainContext );
		assertEquals( 10, retrievedAgainContext.getId() );
		assertEquals( "the_application_code", retrievedAgainContext.getCode() );
	}
}
