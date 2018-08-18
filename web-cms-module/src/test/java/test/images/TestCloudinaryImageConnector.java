/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.images;

import com.cloudinary.Cloudinary;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.connector.CloudinaryWebCmsImageConnector;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.test.AcrossTestContext;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import test.images.TestCustomImageConnector.CustomImageConnectorConfiguration;

import java.util.List;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static test.images.TestCustomImageConnector.BEAN_NAME;
import static test.images.TestCustomImageConnector.PROXY_BEAN_NAME;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestCloudinaryImageConnector
{
	@Test
	public void cloudinaryIsUsedToCreateConnector() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .register( CloudinaryConfiguration.class )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class, true );
			assertEquals( 2, connectors.size() );

			WebCmsImageConnector proxy = ctx.getBeanOfType( WebCmsImageConnector.class );
			assertSame( proxy, ctx.getBean( PROXY_BEAN_NAME ) );

			CloudinaryWebCmsImageConnector target = ctx.getBeanFromModule( WebCmsModule.NAME, BEAN_NAME );
			assertNotNull( target );
			Cloudinary cloudinary = ctx.getBeanOfType( Cloudinary.class );
			assertNotNull( cloudinary );
		}
	}

	@Test
	public void cloudinaryIsCreatedAndExposedIfPropertiesSet() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .property( "web-cms-module.images.cloudinary.enabled", "true" )
		                                  .property( "web-cms-module.images.cloudinary.cloudName", "someCloudName" )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class, true );
			assertEquals( 2, connectors.size() );

			WebCmsImageConnector proxy = ctx.getBeanOfType( WebCmsImageConnector.class );
			assertSame( proxy, ctx.getBean( PROXY_BEAN_NAME ) );

			CloudinaryWebCmsImageConnector target = ctx.getBeanFromModule( WebCmsModule.NAME, BEAN_NAME );
			assertNotNull( target );

			Cloudinary cloudinary = ctx.getBeanOfType( Cloudinary.class );
			assertNotNull( cloudinary );
			assertNotNull( cloudinary.url() );
		}
	}

	@Test
	public void anExistingImageConnectorIsUsedIfPresent() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .register( CustomImageConnectorConfiguration.class, CloudinaryConfiguration.class )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class, true );
			assertEquals( 1, connectors.size() );
			assertFalse( ctx.containsBean( PROXY_BEAN_NAME ) );
			assertSame( connectors.get( 0 ), ctx.getBean( BEAN_NAME ) );
		}
	}

	@Test
	public void specificallyCreatedImageConnectorIsUsedIfPresent() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .modules( new EmptyAcrossModule( "ConnectorModule", CustomImageConnectorConfiguration.class ) )
		                                  .modules( new EmptyAcrossModule( "CloudinaryModule", CloudinaryConfiguration.class ) )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class, true );
			assertEquals( 2, connectors.size() );

			WebCmsImageConnector proxy = ctx.getBeanOfType( WebCmsImageConnector.class );
			assertSame( proxy, ctx.getBean( PROXY_BEAN_NAME ) );

			WebCmsImageConnector target = ctx.getBean( BEAN_NAME, WebCmsImageConnector.class );

			WebCmsImage image = new WebCmsImage();
			proxy.buildImageUrl( image, 100, 50 );
			verify( target ).buildImageUrl( image, 100, 50 );
		}
	}

	@Configuration
	static class CloudinaryConfiguration
	{
		@Bean
		@Exposed
		public Cloudinary cloudinary() {
			return mock( Cloudinary.class );
		}
	}
}
