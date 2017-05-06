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

package com.foreach.across.modules.webcms.domain.publication.config;

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import com.foreach.across.modules.webcms.domain.publication.web.PublicationTypeFormProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;

import java.util.Set;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Configuration
@RequiredArgsConstructor
class WebCmsPublicationConfiguration implements EntityConfigurer
{
	private final PublicationTypeFormProcessor publicationTypeFormProcessor;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsPage.class )
		        .association( ab -> ab.name( "webCmsPublication.articleTemplatePage" ).hide() );

		entities.withType( WebCmsPublicationType.class )
		        .properties(
				        props -> props.property( "articleTypes" )
				                      .displayName( "Article types" )
				                      .propertyType( TypeDescriptor.collection( Set.class, TypeDescriptor.valueOf( WebCmsArticleType.class ) ) )
				                      .attribute( EntityAttributes.CONTROL_NAME, PublicationTypeFormProcessor.ARTICLE_TYPES_CONTROL_NAME )
				                      .valueFetcher( publicationTypeFormProcessor )
				                      .writable( true )
				                      .readable( false )
		        )
		        .createOrUpdateFormView( fvb -> fvb.viewProcessor( publicationTypeFormProcessor ) );

		entities.withType( WebCmsPublication.class )
		        .properties(
				        props -> props
						        .property( "objectId" ).order( 0 ).writable( false ).and()
						        .property( "publish-settings" ).hidden( true ).and()
						        .property( "published" ).hidden( false )
		        );
	}
}
