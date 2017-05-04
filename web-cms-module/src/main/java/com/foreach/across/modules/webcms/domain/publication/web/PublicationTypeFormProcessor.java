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
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.support.ValueFetcher;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	private final WebCmsTypeSpecifierLinkRepository typeLinkRepository;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		command.addExtension( "articleTypes", new ArticleTypesHolder() );
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() ) {
			WebCmsObject owner = command.getEntity( WebCmsObject.class );
			ArticleTypesHolder typesHolder = command.getExtension( "articleTypes", ArticleTypesHolder.class );

			Map<WebCmsTypeSpecifier, WebCmsTypeSpecifierLink> currentLinks
					= typeLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( owner.getObjectId(), null )
					                    .stream()
					                    .collect( Collectors.toMap( WebCmsTypeSpecifierLink::getTypeSpecifier, Function.identity() ) );

			typesHolder.getAllowed().forEach( articleType -> {
				if ( currentLinks.remove( articleType ) == null ) {
					WebCmsTypeSpecifierLink link = new WebCmsTypeSpecifierLink();
					link.setOwner( owner );
					link.setTypeSpecifier( articleType );
					link.setLinkType( WebCmsArticleType.OBJECT_TYPE );

					typeLinkRepository.save( link );
				}
			} );

			currentLinks.values().forEach( typeLinkRepository::delete );
		}
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		ContainerViewElementUtils.move( container, "formGroup-articleTypes", SingleEntityFormViewProcessor.RIGHT_COLUMN );
	}

	@Override
	public Object getValue( WebCmsObject entity ) {
		if ( entityViewRequest.getHttpMethod() == HttpMethod.POST && entityViewRequest.getCommand().hasExtension( "articleTypes" ) ) {
			return entityViewRequest.getCommand().getExtension( "articleTypes", ArticleTypesHolder.class ).allowed;
		}

		return entity.isNew()
				? Collections.emptySet()
				: typeLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( entity.getObjectId(), WebCmsArticleType.OBJECT_TYPE )
				                    .stream()
				                    .map( WebCmsTypeSpecifierLink::getTypeSpecifier )
				                    .collect( Collectors.toSet() );
	}

	@Data
	class ArticleTypesHolder
	{
		Set<WebCmsArticleType> allowed = new HashSet<>();
	}
}


