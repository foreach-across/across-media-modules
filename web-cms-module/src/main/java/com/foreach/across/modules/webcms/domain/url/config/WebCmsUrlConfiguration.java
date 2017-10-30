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

package com.foreach.across.modules.webcms.domain.url.config;

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.ListFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.SortableTableRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.web.WebCmsUrlEndpointViewElementBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.EnumSet;

/**
 * Base configuration for the {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} entity.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
class WebCmsUrlConfiguration
{
	@Autowired
	void registerSingleValuePropertyConverters( WebCmsDataConversionService conversionService ) {
		conversionService.registerSingleValueProperty( WebCmsUrl.class, "httpStatus" );
		conversionService.addConverter( Integer.class, HttpStatus.class, HttpStatus::valueOf );
	}

	@ConditionalOnAdminUI
	@Configuration
	@RequiredArgsConstructor
	static class AdminUiConfiguration implements EntityConfigurer
	{
		private final WebCmsUrlEndpointViewElementBuilder endpointViewElementBuilder;

		@Override
		public void configure( EntitiesConfigurationBuilder entities ) {
			entities.withType( WebCmsUrl.class )
			        .attribute( WebCmsEntityAttributes.DOMAIN_PROPERTY, "endpoint.domain" )
			        .properties(
					        props -> props.property( "httpStatus" )
					                      .attribute(
							                      EntityAttributes.OPTIONS_ALLOWED_VALUES,
							                      EnumSet.of( HttpStatus.OK, HttpStatus.MOVED_PERMANENTLY, HttpStatus.FOUND, HttpStatus.NOT_FOUND )
					                      )
					                      .and()
					                      .property( "endpoint" )
					                      .viewElementBuilder( ViewElementMode.LIST_VALUE, endpointViewElementBuilder )
			        )
			        .listView(
					        lvb -> lvb.defaultSort( "path" )
					                  .showProperties( "path", "httpStatus", "endpoint" )
					                  .sortableOn( "path", "httpStatus" )
					                  .entityQueryFilter( true )
					                  .pageSize( 50 )
					                  .postProcess( ListFormViewProcessor.class, listFormViewProcessor -> listFormViewProcessor.setAddDefaultButtons( false ) )
					                  .postProcess( SortableTableRenderingViewProcessor.class, table -> table.setIncludeDefaultActions( false ) )
					                  .viewProcessor( new EntityViewProcessorAdapter()
					                  {
						                  @Override
						                  protected void render( EntityViewRequest entityViewRequest,
						                                         EntityView entityView,
						                                         ContainerViewElementBuilderSupport<?, ?> containerBuilder,
						                                         ViewElementBuilderMap builderMap,
						                                         ViewElementBuilderContext builderContext ) {
							                  entityViewRequest.getPageContentStructure().setPageTitle( "URL path browser" );
						                  }
					                  } )
			        );
		}
	}
}
