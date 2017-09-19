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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntityConfigurationBuilder;
import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQueryCondition;
import com.foreach.across.modules.entity.query.EntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQueryOps;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityFactory;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.asset.QWebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainAdminUiService;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.url.config.WebCmsAssetUrlConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows configuration of a {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem} items for a {@link WebCmsAsset}.
 * Requires the asset to also have a single {@link com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint}.
 *
 * @author Arne Vandamme
 * @see WebCmsAssetUrlConfiguration
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Exposed
@Configuration
@RequiredArgsConstructor
public class WebCmsAssetMenuViewsConfiguration
{
	private final Set<Class<?>> assetTypes = new HashSet<>();
	private final EntityRegistry entityRegistry;
	private final WebCmsAssetEndpointRepository assetEndpointRepository;
	private final WebCmsMultiDomainService multiDomainService;
	private final WebCmsMultiDomainAdminUiService multiDomainAdminUiService;

	/**
	 * Enable {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem} management for a specific {@link WebCmsAsset} implementation.
	 * Note: this method should be called before the actual entity configuration happens by {@link com.foreach.across.modules.entity.EntityModule}.
	 *
	 * @param assetType to enable
	 */
	public void enable( Class<? extends WebCmsAsset> assetType ) {
		assetTypes.add( assetType );
	}

	/**
	 * Disable {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem} management for a specific {@link WebCmsAsset} implementation.
	 * Note: this method should be called before the actual entity configuration happens by {@link com.foreach.across.modules.entity.EntityModule}.
	 *
	 * @param assetType to disable
	 */
	public void disable( Class<? extends WebCmsAsset> assetType ) {
		assetTypes.remove( assetType );
	}

	/**
	 * Manually register a {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem} association on the configuration builder.
	 * <p/>
	 * Name of the association is <strong>webCmsMenuItems</strong>.
	 *
	 * @param configuration builder
	 */
	public void registerMenuItemsAssociation( EntityConfigurationBuilder<?> configuration ) {
		configuration
				.association(
						ab -> ab
								.name( "webCmsMenuItems" )
								.targetEntityType( WebCmsMenuItem.class )
								.targetProperty( "endpoint" )
								.associationType( EntityAssociation.Type.EMBEDDED )
								.parentDeleteMode( EntityAssociation.ParentDeleteMode.WARN )
								.listView( fvb -> fvb.showProperties( ".", "~endpoint" ).defaultSort( new Sort( "menu", "path" ) ) )
								.createFormView( fvb -> fvb.showProperties( ".", "~endpoint" ) )
								.updateFormView( fvb -> fvb.showProperties( ".", "~endpoint" ) )
								.deleteFormView()
								.attribute( EntityFactory.class, createWebCmsMenuItemEntityFactory() )
								.show()
				)
				.postProcessor(
						cfg -> cfg.association( "webCmsMenuItems" )
						          .setAttribute( AssociatedEntityQueryExecutor.class, createExecutorForWebCmsMenuItem() )
				);
	}

	@SuppressWarnings("unchecked")
	private AssociatedEntityQueryExecutor<WebCmsMenuItem> createExecutorForWebCmsMenuItem() {
		EntityConfiguration<WebCmsMenuItem> entityConfiguration = entityRegistry.getEntityConfiguration( WebCmsMenuItem.class );
		val entityQueryExecutor = entityConfiguration.getAttribute( EntityQueryExecutor.class );

		return new AssociatedEntityQueryExecutor<WebCmsMenuItem>( null, entityQueryExecutor )
		{
			@Override
			protected EntityQueryCondition buildEqualsOrContainsCondition( Object value ) {
				QWebCmsAssetEndpoint query = QWebCmsAssetEndpoint.webCmsAssetEndpoint;
				val endpoint = assetEndpointRepository.findAll(
						multiDomainAdminUiService.applyVisibleDomainsPredicate( query.asset.eq( (WebCmsAsset) value ), query.domain )
				);
				return new EntityQueryCondition( "endpoint", EntityQueryOps.IN, endpoint.toArray() );
			}
		};
	}

	private EntityFactory<WebCmsMenuItem> createWebCmsMenuItemEntityFactory() {
		return new EntityFactory<WebCmsMenuItem>()
		{
			@Override
			public WebCmsMenuItem createNew( Object... args ) {
				WebCmsMenuItem item = new WebCmsMenuItem();
				if ( args[0] instanceof WebCmsAsset ) {
					WebCmsAsset asset = (WebCmsAsset) args[0];
					item.setEndpoint( assetEndpointRepository.findOneByAssetAndDomain( asset, multiDomainService.getCurrentDomainForEntity( asset ) ) );
				}

				return item;
			}

			@Override
			public WebCmsMenuItem createDto( WebCmsMenuItem entity ) {
				return entity.toDto();
			}
		};
	}

	@Bean
	EntityConfigurer webCmsMenuItemsAssociationConfigurer() {
		return entities -> registerMenuItemsAssociation(
				entities.matching(
						config -> WebCmsAsset.class.isAssignableFrom( config.getEntityType() ) && assetTypes.contains( config.getEntityType() )
				)
		);
	}
}
