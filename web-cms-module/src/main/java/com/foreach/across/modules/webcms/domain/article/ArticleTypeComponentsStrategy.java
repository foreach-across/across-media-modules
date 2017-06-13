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

package com.foreach.across.modules.webcms.domain.article;

import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Builds the default article components based on the article template that is attached to the {@link WebCmsArticleType}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
final class ArticleTypeComponentsStrategy implements WebCmsArticleComponentsStrategy
{
	public static final String DEFAULT_CONTENT_COMPONENT = "content";

	private final WebCmsComponentModelService componentModelService;
	private final WebCmsArticleTypeRepository articleTypeRepository;

	@Override
	public void createDefaultComponents( WebCmsArticle article ) {
		WebCmsArticleType articleType = article.getArticleType();
		WebCmsComponentModel template = retrieveContentTemplate( articleType );

		if ( template != null ) {
			template = template.asComponentTemplate();
			template.setOwner( article );
			replaceAttributesInTextComponents( article, template );
			componentModelService.save( template );
		}
	}

	// replace the @@title@@ placeholder
	private void replaceAttributesInTextComponents( WebCmsArticle article, WebCmsComponentModel componentModel ) {
		if ( componentModel instanceof TextWebCmsComponentModel ) {
			TextWebCmsComponentModel text = (TextWebCmsComponentModel) componentModel;
			text.setContent( StringUtils.replace( text.getContent(), "@@title@@", article.getTitle() ) );
			text.setContent( StringUtils.replace( text.getContent(), "@@subTitle@@", article.getSubTitle() ) );
			text.setContent( StringUtils.replace( text.getContent(), "@@description@@", article.getDescription() ) );
		}
		else if ( componentModel instanceof ContainerWebCmsComponentModel ) {
			( (ContainerWebCmsComponentModel) componentModel ).getMembers().forEach( m -> replaceAttributesInTextComponents( article, m ) );
		}
	}

	private WebCmsComponentModel retrieveContentTemplate( WebCmsArticleType articleType ) {
		String contentTemplateName = StringUtils.defaultString( articleType.getAttribute( "contentTemplate" ), DEFAULT_CONTENT_COMPONENT );
		WebCmsComponentModel model = componentModelService.getComponentModelByName( contentTemplateName, articleType );

		if ( model == null && articleType.hasAttribute( "parent" ) ) {
			WebCmsArticleType parentArticleType = articleTypeRepository.findOneByTypeKey( articleType.getAttribute( "parent" ) );
			if ( parentArticleType != null ) {
				return retrieveContentTemplate( parentArticleType );
			}
		}

		return model;
	}
}
