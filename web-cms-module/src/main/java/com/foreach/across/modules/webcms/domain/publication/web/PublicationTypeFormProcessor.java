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

package com.foreach.across.modules.webcms.domain.publication.web;

import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.support.ValueFetcher;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class PublicationTypeFormProcessor extends EntityViewProcessorAdapter implements ValueFetcher<WebCmsObject>
{
	public static final String ARTICLE_TYPES_CONTROL_NAME = "extensions[articleTypes].allowed";

	private final EntityViewRequest entityViewRequest;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		command.addExtension( "articleTypes", new ArticleTypesHolder() );
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		/*container.find( "formGroup-articleTypes", FormGroupElement.class )
		         .ifPresent(
				         group -> group.findAll( CheckboxFormElement.class,
				                                 checkbox -> StringUtils.startsWith( checkbox.getControlName(), "entity." + ARTICLE_TYPES_CONTROL_NAME ) )
				                       .forEach( checkbox -> checkbox.setControlName( ARTICLE_TYPES_CONTROL_NAME ) )
		         );*/
	}

	@Override
	public Object getValue( WebCmsObject entity ) {
		if ( entityViewRequest.getHttpMethod() == HttpMethod.POST && entityViewRequest.getCommand().hasExtension( "articleTypes" ) ) {
			return entityViewRequest.getCommand().getExtension( "articleTypes", ArticleTypesHolder.class ).allowed;
		}

		return Collections.emptySet();
	}

	@Data
	class ArticleTypesHolder
	{
		Set<WebCmsArticleType> allowed = new HashSet<>();
	}
}


