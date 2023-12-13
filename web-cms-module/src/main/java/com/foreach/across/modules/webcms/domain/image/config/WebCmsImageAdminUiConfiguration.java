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

import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.query.EntityQueryConditionTranslator;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.processors.ListFormViewProcessor;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.asset.web.builders.ImageUploadViewElementBuilder;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.web.WebCmsImageFormViewProcessor;
import com.foreach.across.modules.webcms.domain.image.web.WebCmsImageListViewProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Sort;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Configuration
@RequiredArgsConstructor
class WebCmsImageAdminUiConfiguration implements EntityConfigurer
{
	private final ImageUploadViewElementBuilder thumbnailViewElementBuilder;
	private final WebCmsImageFormViewProcessor imageFormViewProcessor;
	private final WebCmsImageListViewProcessor listViewProcessor;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsImage.class )
		        .properties(
				        props -> props.property( "publish-settings" ).hidden( true ).and()
				                      .property( "objectId" ).hidden( true ).and()
				                      .property( "externalId" ).writable( false ).and()
				                      .property( "image-asset" )
				                      .displayName( "Image" )
				                      .hidden( true )
				                      .writable( true )
				                      .readable( false )
				                      .viewElementBuilder( ViewElementMode.CONTROL, thumbnailViewElementBuilder ).and()
				                      .property( "name" )
				                      .attribute( EntityQueryConditionTranslator.class, EntityQueryConditionTranslator.ignoreCase() ).and()
				                      .property( "description" )
				                      .attribute( EntityQueryConditionTranslator.class, EntityQueryConditionTranslator.ignoreCase() ).and()
				                      .property( "source" )
				                      .attribute( EntityQueryConditionTranslator.class, EntityQueryConditionTranslator.ignoreCase() ).and()
				                      .property( "keywords" )
				                      .attribute( EntityQueryConditionTranslator.class, EntityQueryConditionTranslator.ignoreCase() ).and()
				                      .property( "text" )
				                      .propertyType( TypeDescriptor.valueOf( String.class ) )
				                      .attribute( EntityQueryConditionTranslator.class,
				                                  EntityQueryConditionTranslator.expandingOr( "name", "description", "source", "keywords" ) )
		        )
		        .createOrUpdateFormView(
				        fvb -> fvb.showProperties( ".", "image-asset" )
				                  .viewProcessor( imageFormViewProcessor )
		        )
		        .updateFormView(
				        fvb -> fvb.showProperties( ".", "externalId", "image-asset" )
		        )
		        .listView(
				        lvb -> lvb.viewProcessor( listViewProcessor )
				                  .entityQueryFilter( eq -> eq.basicMode( true ).showProperties( "text" ) )
				                  .defaultSort( Sort.by( Sort.Direction.DESC, "publicationDate" ) )
				                  .postProcess( ListFormViewProcessor.class,
				                                listFormViewProcessor -> listFormViewProcessor.setAddDefaultButtons( false ) )
		        );
	}
}
