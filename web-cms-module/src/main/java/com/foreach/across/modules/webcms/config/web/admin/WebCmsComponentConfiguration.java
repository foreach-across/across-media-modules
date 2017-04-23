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

package com.foreach.across.modules.webcms.config.web.admin;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.config.builders.EntityConfigurationBuilder;
import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.views.processors.DefaultValidationViewProcessor;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.web.component.WebComponentFormProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.validation.groups.Default;
import java.util.List;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AcrossDepends(required = AdminWebModule.NAME)
@Configuration
@RequiredArgsConstructor
class WebCmsComponentConfiguration implements EntityConfigurer
{
	private final WebComponentFormProcessor formProcessor;

	private AssociatedEntityQueryExecutor<WebCmsComponent> componentAssociatedEntityQueryExecutor;

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
				return new PageImpl<WebCmsComponent>( findAll( parent, query ) );
			}
		};
	}

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsComponentType.class ).hide();

		entities.withType( WebCmsComponent.class )
		        .properties(
				        props -> props.property( "componentType" ).order( 0 ).and()
				                      .property( "title" ).order( 1 ).and()
				                      .property( "name" ).order( 2 ).and()
				                      .property( "ownerObjectId" ).hidden( true ).and()
				                      .property( "sortIndex" ).order( 10 ).hidden( true ).and()
				                      .property( "objectId" ).writable( false ).hidden( true ).and()
				                      .property( "body" ).hidden( true ).and()
				                      .property( "metadata" ).hidden( true )
		        )
		        .listView(
				        lvb -> lvb.entityQueryFilter( true )
				                  .showProperties( "componentType", "title", "name", "lastModified" )
				                  .defaultSort( "title" )
		        )
		        .createOrUpdateFormView(
				        fvb -> fvb.postProcess(
						        DefaultValidationViewProcessor.class,
						        processor -> processor.setValidationHints( Default.class, WebCmsComponent.SharedComponentValidation.class )
				        )
		        )
		        .updateFormView(
				        fvb -> fvb.properties( props -> props.property( "componentType" ).writable( false ) )
				                  .showProperties( "componentType", "title", "name", "lastModified" )
				                  .viewProcessor( formProcessor )
				                  .postProcess( SingleEntityFormViewProcessor.class, processor -> processor.setGrid( Grid.create( 8, 4 ) ) )
		        );

		registerComponentsAssociation( entities.withType( WebCmsPage.class ) );
	}

	private void registerComponentsAssociation( EntityConfigurationBuilder<? extends WebCmsObject> configuration ) {
		configuration.association( ab -> ab
				.name( "components" )
				.targetEntityType( WebCmsComponent.class )
				.targetProperty( "owner" )
				.associationType( EntityAssociation.Type.EMBEDDED )
				.attribute( AssociatedEntityQueryExecutor.class, componentAssociatedEntityQueryExecutor )
				.listView(
						lvb -> lvb.showProperties( "componentType", "title", "name", "sortIndex", "lastModified" )
						          .sortableOn()
				)
				.createOrUpdateFormView( fvb -> fvb.properties( props -> props.property( "sortIndex" ).hidden( false ) ) )
				.updateFormView(
						fvb -> fvb.properties( props -> props.property( "componentType" ).writable( false ) )
						          .showProperties( "componentType", "title", "name", "sortIndex", "lastModified" )
						          .viewProcessor( formProcessor )
						          .postProcess( SingleEntityFormViewProcessor.class, processor -> processor.setGrid( Grid.create( 8, 4 ) ) )
				)
				.deleteFormView()
		);
	}

}
