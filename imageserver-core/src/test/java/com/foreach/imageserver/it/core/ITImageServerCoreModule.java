package com.foreach.imageserver.it.core;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.test.AcrossTestWebConfiguration;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.services.ImageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITImageServerCoreModule.Config.class)
public class ITImageServerCoreModule
{
	@Autowired(required = false)
	private ImageService imageService;

	@Autowired(required = false)
	private MultipartResolver multipartResolver;

	@Autowired
	private DispatcherServlet dispatcherServlet;

	@Test
	public void exposedServices() {
		assertNotNull( imageService );
	}

	@Test
	public void multipartResolverShouldBeDetected() {
		assertNotNull( dispatcherServlet );
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new ImageServerCoreModule() );
			context.addModule( new AcrossHibernateModule() );
		}
	}
}
