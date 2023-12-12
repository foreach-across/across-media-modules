package it;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.foreach.imageserver.core.ImageServerCoreModule.NAME;
import static com.foreach.imageserver.core.config.WebConfiguration.IMAGE_REQUEST_HASH_BUILDER;
import static it.ITImageServerCoreModule.Config;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@TestPropertySource(properties = { "imageServerCore.strictMode=true", "imageServerCore.md5HashToken=test" })
@ContextConfiguration(classes = Config.class)
public class ITImageServerCoreModuleStrictMode
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void noImageRequestHashBuilderShouldBeCreated() {
		assertThrows( NoSuchBeanDefinitionException.class, () -> {
			assertNull( beanRegistry.getBeanFromModule( NAME,
			                                            IMAGE_REQUEST_HASH_BUILDER ) );
		} );
	}
}
