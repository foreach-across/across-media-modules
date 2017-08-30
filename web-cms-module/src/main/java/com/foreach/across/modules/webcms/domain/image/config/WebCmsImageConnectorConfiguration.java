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

package com.foreach.across.modules.webcms.domain.image.config;

import com.cloudinary.Cloudinary;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.core.events.AcrossContextBootstrappedEvent;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.modules.webcms.domain.image.connector.CloudinaryWebCmsImageConnector;
import com.foreach.across.modules.webcms.domain.image.connector.ImageServerWebCmsImageConnector;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.Md5ImageRequestHashBuilder;
import com.foreach.imageserver.client.RemoteImageServerClient;
import liquibase.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

import static com.foreach.across.modules.webcms.domain.image.config.WebCmsImageConnectorConfiguration.BEAN_NAME;

/**
 * Initializes the {@link com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector} if necessary.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = BEAN_NAME)
class WebCmsImageConnectorConfiguration
{
	static final String BEAN_NAME = "webCmsImageConnector";

	private final BeanFactory beanFactory;

	/**
	 * Creates a proxy to the actual {@link WebCmsImageConnector} that should be used.
	 * Expects a bean named {@code webCmsImageConnector} to be available in the bean factory after the context has been bootstrapped
	 * (or before first use of the image connector).
	 *
	 * @return lazy initialisation proxy for the image connector
	 */
	@Bean
	@Exposed
	@Primary
	public WebCmsImageConnector imageConnectorProxy( BeanFactory beanFactory ) {
		LOG.trace( "No WebCmsImageConnector bean present during module bootstrap - creating lazy initialization proxy.  " +
				           "Proxy will attempt to wire a bean named 'webCmsImageConnector' upon first use." );

		LazyInitTargetSource targetSource = new LazyInitTargetSource();
		targetSource.setTargetClass( WebCmsImageConnector.class );
		targetSource.setTargetBeanName( BEAN_NAME );
		targetSource.setBeanFactory( beanFactory );

		return ProxyFactory.getProxy( WebCmsImageConnector.class, targetSource );
	}

	@Event
	@Order
	void verifyExistenceOfActualImageConnectorImplementation( AcrossContextBootstrappedEvent contextBootstrappedEvent ) {
		if ( !beanFactory.containsBean( BEAN_NAME ) ) {
			LOG.warn( "No implementation of WebCmsImageConnector was found - a bean named {} was expected.  " +
					          "WebCmsImage related functionality will fail.", BEAN_NAME );
		}
	}

	/**
	 * If no webCmsImageConnector bean is present yet in the bean factory, will attempt to create one if either an
	 * {@link ImageServerClient} bean is present or the image server client properties are set.
	 */
	@RequiredArgsConstructor
	@Configuration
	@ConditionalOnClass(ImageServerClient.class)
	@EnableConfigurationProperties(ImageServerClientProperties.class)
	static class ImageServerClientAutoConfiguration
	{
		private final ConfigurableListableBeanFactory beanFactory;

		@Exposed
		@Bean
		@ConditionalOnProperty(prefix = "webCmsModule.images.imageServer", value = "enabled", havingValue = "true")
		public ImageServerClient imageServerClient( ImageServerClientProperties properties ) {
			RemoteImageServerClient imageServerClient = new RemoteImageServerClient( properties.getUrl(), properties.getAccessToken() );
			if ( !StringUtils.isEmpty( properties.getHashToken() ) ) {
				imageServerClient.setImageRequestHashBuilder( new Md5ImageRequestHashBuilder( properties.getHashToken() ) );
			}
			return imageServerClient;
		}

		@Event
		@OrderInModule(1)
		void autoConfigureImageServerConnector( AcrossModuleBootstrappedEvent moduleBootstrappedEvent ) {
			autoConfigureImageServerConnector( false );
		}

		@Event
		@OrderInModule(1)
		void autoConfigureImageServerConnector( AcrossContextBootstrappedEvent contextBootstrappedEvent ) {
			autoConfigureImageServerConnector( true );
		}

		private void autoConfigureImageServerConnector( boolean last ) {
			if ( !beanFactory.containsBean( BEAN_NAME ) ) {
				try {
					ImageServerClient imageServerClient = beanFactory.getBean( ImageServerClient.class );
					beanFactory.registerSingleton( BEAN_NAME, new ImageServerWebCmsImageConnector( imageServerClient ) );

					LOG.trace( "Auto-created an ImageServerWebCmsImageConnector using an existing ImageServerClient bean." );
				}
				catch ( NoUniqueBeanDefinitionException nube ) {
					LOG.error( "More than one ImageServerClient bean found and none marked primary - unable to auto-create ImageServerWebCmsImageConnector." );
				}
				catch ( NoSuchBeanDefinitionException nsbe ) {
					if ( last ) {
						LOG.trace( "No ImageServerClient bean found - skipping auto-creation of ImageServerWebCmsImageConnector." );
					}
				}
			}
		}
	}

	/**
	 * If no webCmsImageConnector bean is present yet in the bean factory, will attempt to create one if either a
	 * {@link Cloudinary} bean is present or the cloudinary properties are set.
	 */
	@RequiredArgsConstructor
	@Configuration
	@ConditionalOnClass(Cloudinary.class)
	@EnableConfigurationProperties(CloudinaryProperties.class)
	static class CloudinaryAutoConfiguration
	{
		private final ConfigurableListableBeanFactory beanFactory;

		@Exposed
		@Bean
		@ConditionalOnProperty(prefix = "webCmsModule.images.cloudinary", value = "enabled", havingValue = "true")
		public Cloudinary cloudinary( CloudinaryProperties properties ) {
			Map<String, String> settings = new HashMap<>();
			settings.put( "cloud_name", properties.getCloudName() );
			settings.put( "api_key", properties.getApiKey() );
			settings.put( "api_secret", properties.getApiSecret() );

			return new Cloudinary( settings );
		}

		@Event
		@OrderInModule(1)
		void autoConfigureCloudinaryConnector( AcrossModuleBootstrappedEvent moduleBootstrappedEvent ) {
			autoConfigureCloudinaryConnector( false );
		}

		@Event
		@OrderInModule(1)
		void autoConfigureCloudinaryConnector( AcrossContextBootstrappedEvent contextBootstrappedEvent ) {
			autoConfigureCloudinaryConnector( true );
		}

		private void autoConfigureCloudinaryConnector( boolean last ) {
			if ( !beanFactory.containsBean( BEAN_NAME ) ) {
				try {
					Cloudinary cloudinary = beanFactory.getBean( Cloudinary.class );
					beanFactory.destroyBean( BEAN_NAME );

					beanFactory.registerSingleton( BEAN_NAME, new CloudinaryWebCmsImageConnector( cloudinary ) );

					LOG.trace( "Auto-created an CloudinaryWebCmsImageConnector using an existing Cloudinary bean." );
				}
				catch ( NoUniqueBeanDefinitionException nube ) {
					LOG.error( "More than one Cloudinary bean found and none marked primary - unable to auto-create CloudinaryWebCmsImageConnector." );
				}
				catch ( NoSuchBeanDefinitionException nsbe ) {
					if ( last ) {
						LOG.trace( "No Cloudinary bean found - skipping auto-creation of CloudinaryWebCmsImageConnector." );
					}
				}
			}
		}
	}
}
