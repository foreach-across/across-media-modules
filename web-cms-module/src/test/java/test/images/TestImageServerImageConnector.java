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

import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.connector.ImageServerWebCmsImageConnector;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.test.AcrossTestContext;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.RemoteImageServerClient;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import test.images.TestCustomImageConnector.CustomImageConnectorConfiguration;

import java.util.List;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static test.images.TestCustomImageConnector.BEAN_NAME;
import static test.images.TestCustomImageConnector.PROXY_BEAN_NAME;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestImageServerImageConnector
{
	@Test
	public void imageServerClientIsUsedToCreateConnector() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .register( ImageServerClientConfiguration.class )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class, true );
			assertEquals( 2, connectors.size() );

			WebCmsImageConnector proxy = ctx.getBeanOfType( WebCmsImageConnector.class );
			assertSame( proxy, ctx.getBean( PROXY_BEAN_NAME ) );

			ImageServerWebCmsImageConnector target = ctx.getBeanFromModule( WebCmsModule.NAME, BEAN_NAME );
			assertNotNull( target );
			ImageServerClient imageServerClient = ctx.getBeanOfType( ImageServerClient.class );

			WebCmsImage image = WebCmsImage.builder().externalId( "my-image" ).build();
			proxy.buildImageUrl( image, 100, 50 );

			ImageVariantDto variant = new ImageVariantDto();
			variant.setBoundaries( new DimensionsDto( 100, 50 ) );
			variant.setImageType( ImageTypeDto.PNG );
			verify( imageServerClient ).imageUrl( "my-image", "default", 0, 0, variant );
		}
	}

	@Test
	public void imageServerClientIsCreatedAndExposedIfPropertiesSet() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .property( "web-cms-module.images.image-server.enabled", "true" )
		                                  .property( "web-cms-module.images.image-server.url", "http://my-imageserver" )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class, true );
			assertEquals( 2, connectors.size() );

			WebCmsImageConnector proxy = ctx.getBeanOfType( WebCmsImageConnector.class );
			assertSame( proxy, ctx.getBean( PROXY_BEAN_NAME ) );

			ImageServerWebCmsImageConnector target = ctx.getBeanFromModule( WebCmsModule.NAME, BEAN_NAME );
			assertNotNull( target );

			RemoteImageServerClient imageServerClient = ctx.getBeanOfType( RemoteImageServerClient.class );
			assertNotNull( imageServerClient );
			assertEquals( "http://my-imageserver", imageServerClient.getImageServerUrl() );
		}
	}

	@Test
	public void anExistingImageConnectorIsUsedIfPresent() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .register( CustomImageConnectorConfiguration.class, ImageServerClientConfiguration.class )
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
		                                  .modules( new EmptyAcrossModule( "ImageServerModule", ImageServerClientConfiguration.class ) )
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
	static class ImageServerClientConfiguration
	{
		@Bean
		public ImageServerClient imageServerClient() {
			return mock( ImageServerClient.class );
		}
	}
}
