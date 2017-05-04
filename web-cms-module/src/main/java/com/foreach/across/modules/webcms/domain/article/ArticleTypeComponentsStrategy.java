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
	private final WebCmsComponentModelService componentModelService;
	private final WebCmsArticleTypeRepository articleTypeRepository;

	@Override
	public void createDefaultComponents( WebCmsArticle article ) {
		WebCmsArticleType articleType = article.getArticleType();
		WebCmsComponentModel template = retrieveArticleTemplateComponent( articleType );

		if ( template != null ) {
			template = template.asTemplate();

			if ( template instanceof ContainerWebCmsComponentModel ) {
				ContainerWebCmsComponentModel container = (ContainerWebCmsComponentModel) template;
				container.getMembers().forEach( member -> {
					member.setOwner( article );
					replaceTitleInTextComponents( article.getTitle(), member );
					componentModelService.save( member );
				} );
			}
			else {
				// not sure what to do, just add the template directly
				template.setOwner( article );
				componentModelService.save( template );
			}
		}
	}

	// replace the @@title@@ placeholder
	private void replaceTitleInTextComponents( String title, WebCmsComponentModel componentModel ) {
		if ( componentModel instanceof TextWebCmsComponentModel ) {
			TextWebCmsComponentModel text = (TextWebCmsComponentModel) componentModel;
			text.setContent( StringUtils.replace( text.getContent(), "@@title@@", title ) );
		}
	}

	private WebCmsComponentModel retrieveArticleTemplateComponent( WebCmsArticleType articleType ) {
		String articleTemplateName = StringUtils.defaultString( articleType.getAttribute( "bodyTemplate" ), "bodyTemplate" );
		WebCmsComponentModel model = componentModelService.getComponentModel( articleTemplateName, articleType );

		if ( model == null && articleType.hasAttribute( "parent" ) ) {
			WebCmsArticleType parentArticleType = articleTypeRepository.findOneByTypeKey( articleType.getAttribute( "parent" ) );
			if ( parentArticleType != null ) {
				return retrieveArticleTemplateComponent( parentArticleType );
			}
		}

		return model;
	}
}
