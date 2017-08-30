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
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.test.AcrossTestContext;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestCustomImageConnector
{
	final static String PROXY_BEAN_NAME = "imageConnectorProxy";
	final static String BEAN_NAME = "webCmsImageConnector";

	@Test
	public void byDefaultOnlyTheWebCmsImageConnectorProxyIsCreated() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME ).build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class );
			assertEquals( 1, connectors.size() );
			assertSame( connectors.get( 0 ), ctx.getBean( PROXY_BEAN_NAME ) );
		}
	}

	@Test
	public void anExistingImageConnectorIsUsedIfPresent() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .register( CustomImageConnectorConfiguration.class )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class );
			assertEquals( 1, connectors.size() );
			assertFalse( ctx.containsBean( PROXY_BEAN_NAME ) );
			assertSame( connectors.get( 0 ), ctx.getBean( BEAN_NAME ) );
		}
	}

	@Test
	public void imageConnectorCreatedLaterIsUsedByTheProxy() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME )
		                                  .modules( new EmptyAcrossModule( "ConnectorModule", CustomImageConnectorConfiguration.class ) )
		                                  .build()) {
			List<WebCmsImageConnector> connectors = ctx.getBeansOfType( WebCmsImageConnector.class );
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
	static class CustomImageConnectorConfiguration
	{
		@Bean
		@Exposed
		public WebCmsImageConnector webCmsImageConnector() {
			return mock( WebCmsImageConnector.class );
		}
	}
}
