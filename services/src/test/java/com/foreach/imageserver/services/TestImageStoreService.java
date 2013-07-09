package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.shared.utils.DateUtils;
import com.foreach.test.MockedLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { TestImageStoreService.TestConfig.class }, loader = MockedLoader.class)
public class TestImageStoreService
{
	@Autowired
	private ImageStoreService imageStoreService;

	@Test
	public void generateRelativeImagePath() {
		Image image = new Image();
		image.setDateCreated( DateUtils.parseDate( "2013-07-06 13:35:13" ) );

		String path = imageStoreService.generateRelativeImagePath( image );

		assertEquals( "/2013/07/06/", path );
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageStoreService imageStoreService() {
			return new ImageStoreServiceImpl( "", "" );
		}
	}
}
