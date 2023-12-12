package it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.ImageTransformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartResolver;

import static com.foreach.imageserver.core.ImageServerCoreModule.NAME;
import static com.foreach.imageserver.core.ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER;
import static com.foreach.imageserver.core.ImageServerCoreModuleSettings.ROOT_PATH;
import static com.foreach.imageserver.core.config.WebConfiguration.IMAGE_REQUEST_HASH_BUILDER;
import static it.ITImageServerCoreModule.Config;
import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class ITImageServerCoreModule
{
	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageContextService contextService;

	@Autowired
	private ImageTransformService imageTransformService;

	@Autowired
	private MultipartResolver multipartResolver;

	@Autowired
	@Qualifier("imageServerHandlerMapping")
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

	@Test
	public void noImageRequestHashBuilderShouldBeCreated() {
		assertThrows( NoSuchBeanDefinitionException.class, () -> {
			assertNull( beanRegistry.getBeanFromModule( NAME,
			                                            IMAGE_REQUEST_HASH_BUILDER ) );
		} );
	}

	@Configuration
	@AcrossTestConfiguration(modules = { AcrossWebModule.NAME, PropertiesModule.NAME, FileManagerModule.NAME })
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( imageServerCoreModule() );
		}

		private ImageServerCoreModule imageServerCoreModule() {
			ImageServerCoreModule imageServerCoreModule = new ImageServerCoreModule();
			imageServerCoreModule.setProperty( IMAGE_STORE_FOLDER,
			                                   getProperty( "java.io.tmpdir" ) );
			imageServerCoreModule.setProperty( ROOT_PATH, "/imgsrvr" );

			return imageServerCoreModule;
		}
	}
}
