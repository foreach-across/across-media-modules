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

package com.foreach.across.modules.webcms.domain.component.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntityConfigurationBuilder;
import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentFormProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper to configure the {@link WebCmsComponent} association for a particular {@link WebCmsObject}.
 * This enables the management UI for components on that asset type.
 * Use {@link #registerComponentsAssociation(EntityConfigurationBuilder)} to configure the <strong>webCmsComponents</strong> association.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Exposed
@Configuration
@RequiredArgsConstructor
public class WebCmsObjectComponentViewsConfiguration
{
	private final WebCmsComponentFormProcessor formProcessor;
	private final Set<Class<?>> assetTypes = new HashSet<>();

	private AssociatedEntityQueryExecutor<WebCmsComponent> componentAssociatedEntityQueryExecutor;

	/**
	 * Enable the web components UI for a specific {@link WebCmsObject} implementation.
	 * Note: this method should be called before the actual entity configuration happens by {@link com.foreach.across.modules.entity.EntityModule}.
	 *
	 * @param assetType to enable
	 */
	public void enable( Class<? extends WebCmsObject> assetType ) {
		assetTypes.add( assetType );
	}

	/**
	 * Disable the web components UI for a specific {@link WebCmsObject} implementation.
	 * Note: this method should be called before the actual entity configuration happens by {@link com.foreach.across.modules.entity.EntityModule}.
	 *
	 * @param assetType to disable
	 */
	public void disable( Class<? extends WebCmsObject> assetType ) {
		assetTypes.remove( assetType );
	}

	@Autowired
	void createExecutorForWebCmsObject( WebCmsComponentRepository componentRepository ) {
		componentAssociatedEntityQueryExecutor = new AssociatedEntityQueryExecutor<WebCmsComponent>( null, null )
		{
			@Override
			public List<WebCmsComponent> findAll( Object parent, EntityQuery query ) {
				return componentRepository.findAllByOwnerObjectIdOrderBySortIndexAsc( ( (WebCmsObject) parent ).getObjectId() );
			}

			@Override
			public Page<WebCmsComponent> findAll( Object parent, EntityQuery query, Pageable pageable ) {
				return new PageImpl<>( findAll( parent, query ) );
			}
		};
	}

	/**
	 * Manually register a {@link WebCmsComponent} association on the configuration builder.
	 * Will add the association as embedded and will not suppress delete but warn about linked components.
	 * <p/>
	 * Name of the association is <strong>webCmsComponents</strong>.
	 *
	 * @param configuration builder
	 */
	public void registerComponentsAssociation( EntityConfigurationBuilder<?> configuration ) {
		configuration.association( ab -> ab
				.name( "webCmsComponents" )
				.targetEntityType( WebCmsComponent.class )
				.targetProperty( "owner" )
				.associationType( EntityAssociation.Type.EMBEDDED )
				.parentDeleteMode( EntityAssociation.ParentDeleteMode.WARN )
				.attribute( AssociatedEntityQueryExecutor.class, componentAssociatedEntityQueryExecutor )
				.listView(
						lvb -> lvb.showProperties( "title", "name", "componentType", "sortIndex", "lastModified" )
						          .sortableOn()
				)
				.createOrUpdateFormView( fvb -> fvb.properties( props -> props.property( "sortIndex" ).hidden( false ) ) )
				.updateFormView(
						fvb -> fvb.properties( props -> props.property( "componentType" ).writable( false ) )
						          .showProperties()
						          .viewProcessor( formProcessor )
						          .postProcess( SingleEntityFormViewProcessor.class, processor -> processor.setGrid( Grid.create( 8, 4 ) ) )
				)
				.deleteFormView()
		);
	}

	@Bean
	EntityConfigurer webCmsComponentsAssociationConfigurer() {
		return entities -> registerComponentsAssociation(
				entities.matching(
						config -> WebCmsObject.class.isAssignableFrom( config.getEntityType() ) && assetTypes.contains( config.getEntityType() )
				)
		);
	}
}
