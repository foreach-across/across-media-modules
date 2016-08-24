package com.foreach.imageserver.it.core;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.across.test.AcrossTestWebConfiguration;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.config.WebConfiguration;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.ImageTransformService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartResolver;

import static org.junit.Assert.*;

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
	private ImageContextService contextService;

	@Autowired(required = false)
	private ImageTransformService imageTransformService;

	@Autowired(required = false)
	private MultipartResolver multipartResolver;

	@Autowired(required = false)
	private PrefixingRequestMappingHandlerMapping imageServerHandlerMapping;

	@Autowired(required = false)
	private ImageServerClient imageServerClient;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void exposedServices() {
		assertNotNull( imageService );
		assertNotNull( contextService );
		assertNotNull( imageTransformService );
	}

	@Test
	public void exposedHandlerMapping() {
		assertNotNull( imageServerHandlerMapping );
		assertFalse( imageServerHandlerMapping.getHandlerMethods().isEmpty() );
		assertEquals( "/imgsrvr", imageServerHandlerMapping.getPrefixPath() );
	}

	@Test
	public void multipartResolverShouldBeCreated() {
		assertNotNull( multipartResolver );
	}

	@Test
	public void noClientShouldBeCreated() {
		assertNull( imageServerClient );
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void noImageRequestHashBuilderShouldBeCreated() {
		assertNull( beanRegistry.getBeanFromModule( ImageServerCoreModule.NAME,
		                                            WebConfiguration.IMAGE_REQUEST_HASH_BUILDER ) );
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( imageServerCoreModule() );
			context.addModule( new AcrossHibernateJpaModule() );
		}

		private ImageServerCoreModule imageServerCoreModule() {
			ImageServerCoreModule imageServerCoreModule = new ImageServerCoreModule();
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
			                                   System.getProperty( "java.io.tmpdir" ) );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.ROOT_PATH, "/imgsrvr" );

			return imageServerCoreModule;
		}
	}
}
