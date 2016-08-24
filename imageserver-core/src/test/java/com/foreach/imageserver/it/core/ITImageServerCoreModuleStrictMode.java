package com.foreach.imageserver.it.core;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.config.WebConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNull;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@TestPropertySource(properties = { "imageServerCore.strictMode=true", "imageServerCore.md5HashToken=test" })
@ContextConfiguration(classes = ITImageServerCoreModule.Config.class)
public class ITImageServerCoreModuleStrictMode
{
	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void noImageRequestHashBuilderShouldBeCreated() {
		assertNull( beanRegistry.getBeanFromModule( ImageServerCoreModule.NAME,
		                                            WebConfiguration.IMAGE_REQUEST_HASH_BUILDER ) );
	}
}
