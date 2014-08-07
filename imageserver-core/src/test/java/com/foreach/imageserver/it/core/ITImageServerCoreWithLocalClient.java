package com.foreach.imageserver.it.core;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.test.AcrossTestWebConfiguration;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = ITImageServerCoreWithLocalClient.Config.class)
public class ITImageServerCoreWithLocalClient
{
	@Autowired(required = false)
	private ImageServerClient imageServerClient;

	@Test
	public void noClientShouldBeCreated() {
		assertNotNull( imageServerClient );
		assertEquals( "http://somehost/img", imageServerClient.getImageServerUrl() );
	}

	@Configuration
	@AcrossTestWebConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( imageServerCoreModule() );
			context.addModule( new AcrossHibernateModule() );
		}

		private ImageServerCoreModule imageServerCoreModule() {
			ImageServerCoreModule imageServerCoreModule = new ImageServerCoreModule();
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
			                                   System.getProperty( "java.io.tmpdir" ) );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.ROOT_PATH, "/imgsrvr" );

			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.CREATE_LOCAL_CLIENT, true );
			imageServerCoreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_SERVER_URL, "http://somehost/img" );

			return imageServerCoreModule;
		}
	}

}
