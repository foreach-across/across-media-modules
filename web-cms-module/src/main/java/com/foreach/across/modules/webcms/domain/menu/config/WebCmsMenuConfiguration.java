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

package com.foreach.across.modules.webcms.domain.menu.config;

import com.foreach.across.modules.bootstrapui.elements.TextboxFormElement;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.entity.web.EntityViewModel;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Configuration
@RequiredArgsConstructor
class WebCmsMenuConfiguration implements EntityConfigurer
{
	private final EndpointViewElementBuilder endpointViewElementBuilder;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsMenuItem.class )
		        .attribute( WebCmsEntityAttributes.DOMAIN_PROPERTY, "menu.domain" )
		        .properties(
				        props -> props.property( "url" ).attribute( TextboxFormElement.Type.class, TextboxFormElement.Type.TEXT ).and()
				                      .property( "endpoint" )
				                      .viewElementBuilder( ViewElementMode.LIST_VALUE, endpointViewElementBuilder )
				                      .viewElementBuilder( ViewElementMode.VALUE, endpointViewElementBuilder )
		        )
		        .hide();

		entities.withType( WebCmsMenu.class )
		        .association(
				        ab -> ab.name( "webCmsMenuItem.menu" )
				                .show()
				                .associationType( EntityAssociation.Type.EMBEDDED )
				                .listView( lvb -> lvb.showProperties( "path", "group", "title", "endpoint", "sortIndex" )
				                                     .defaultSort( Sort.by( "sortIndex", "path" ) ) )
				                .createOrUpdateFormView(
						                fvb -> fvb.showProperties( "group", "*", "~menu" )
				                )
		        );
	}

	@ConditionalOnAdminUI
	@Component
	@RequiredArgsConstructor
	static class EndpointViewElementBuilder implements ViewElementBuilder<ViewElement>
	{
		private final EntityRegistry entityRegistry;

		@Override
		public ViewElement build( ViewElementBuilderContext viewElementBuilderContext ) {
			val url = EntityViewElementUtils.currentEntity( viewElementBuilderContext, WebCmsMenuItem.class );
			val endpoint = url.getEndpoint();

			if ( endpoint != null ) {
				Class<?> actualType = Hibernate.getClass( endpoint );
				val endpointConfiguration = entityRegistry.getEntityConfiguration( actualType );

				if ( endpointConfiguration != null && endpointConfiguration.hasAttribute( "endpointValueBuilder" ) ) {
					val nestedBuilderContext = new DefaultViewElementBuilderContext( viewElementBuilderContext );
					nestedBuilderContext.setAttribute( EntityViewModel.ENTITY, actualEntity( endpoint ) );

					return endpointConfiguration.getAttribute( "endpointValueBuilder", ViewElementBuilder.class ).build( nestedBuilderContext );
				}
			}

			return new ContainerViewElement();
		}

		private WebCmsEndpoint actualEntity( WebCmsEndpoint endpoint ) {
			if ( endpoint instanceof HibernateProxy ) {
				return (WebCmsEndpoint) ( (HibernateProxy) endpoint ).getHibernateLazyInitializer().getImplementation();
			}

			return endpoint;
		}
	}
}
