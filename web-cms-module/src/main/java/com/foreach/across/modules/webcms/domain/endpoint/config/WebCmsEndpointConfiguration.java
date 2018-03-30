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

package com.foreach.across.modules.webcms.domain.endpoint.config;

import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Printer;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Configuration
@RequiredArgsConstructor
class WebCmsEndpointConfiguration implements EntityConfigurer
{
	private final EndpointPrinter endpointPrinter;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsEndpoint.class )
		        .entityModel( model -> model.labelPrinter( endpointPrinter ) );
	}

	@ConditionalOnAdminUI
	@Component
	@RequiredArgsConstructor
	static class EndpointPrinter implements Printer<WebCmsEndpoint>
	{
		private final EntityRegistry entityRegistry;

		@SuppressWarnings("unchecked")
		@Override
		public String print( WebCmsEndpoint object, Locale locale ) {
			Class<?> actualType = Hibernate.getClass( object );

			EntityConfiguration baseConfig = entityRegistry.getEntityConfiguration( WebCmsEndpoint.class );
			EntityConfiguration actualConfig = entityRegistry.getEntityConfiguration( actualType );

			if ( baseConfig != actualConfig ) {
				return actualConfig.getEntityModel().getLabel( actualEntity( object ), locale );
			}

			return object.toString();
		}

		private WebCmsEndpoint actualEntity( WebCmsEndpoint endpoint ) {
			if ( endpoint instanceof HibernateProxy ) {
				return (WebCmsEndpoint) ( (HibernateProxy) endpoint ).getHibernateLazyInitializer().getImplementation();
			}

			return endpoint;
		}
	}
}
