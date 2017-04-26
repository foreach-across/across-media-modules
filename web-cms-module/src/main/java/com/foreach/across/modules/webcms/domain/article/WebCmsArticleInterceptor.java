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

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationTypeRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Add the article type components.
 * Generate primary url when article is being saved.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsArticleInterceptor extends EntityInterceptorAdapter<WebCmsArticle>
{
	private final WebCmsAssetEndpointRepository endpointRepository;
	private final WebCmsUrlRepository urlRepository;
	private final WebCmsComponentModelService componentModelService;
	private final WebCmsPublicationTypeRepository publicationTypeRepository;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsArticle.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterCreate( WebCmsArticle entity ) {
		registerArticleTypeComponents( entity );
		updatePrimaryUrl( entity );
	}

	private void registerArticleTypeComponents( WebCmsArticle article ) {
		WebCmsPublicationType publicationType = article.getPublication().getPublicationType();
		WebCmsComponentModel template = retrieveArticleTemplateComponent( publicationType );

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

	private WebCmsComponentModel retrieveArticleTemplateComponent( WebCmsPublicationType publicationType ) {
		String articleTemplateName = StringUtils.defaultString( publicationType.getAttribute( "articleTemplate" ), "articleTemplate" );
		WebCmsComponentModel model = componentModelService.getComponentModel( articleTemplateName, publicationType );

		if ( model == null && publicationType.hasAttribute( "parent" ) ) {
			WebCmsPublicationType parentPublication = publicationTypeRepository.findOneByTypeKey( publicationType.getAttribute( "parent" ) );
			if ( parentPublication != null ) {
				return retrieveArticleTemplateComponent( parentPublication );
			}
		}

		return model;
	}

	@Override
	public void afterUpdate( WebCmsArticle entity ) {
		updatePrimaryUrl( entity );
	}

	private void updatePrimaryUrl( WebCmsArticle article ) {
		if ( article.isPublished() ) {
			WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( article );

			WebCmsUrl newPrimaryUrl = new WebCmsUrl();
			newPrimaryUrl.setPath( generateUrl( article ) );
			newPrimaryUrl.setHttpStatus( HttpStatus.OK );
			newPrimaryUrl.setPrimary( true );
			newPrimaryUrl.setEndpoint( endpoint );

			WebCmsUrl existing = endpoint.getUrlWithPath( newPrimaryUrl.getPath() ).orElse( null );

			if ( existing != null && !existing.isPrimary() ) {
				newPrimaryUrl = existing.toDto();
				newPrimaryUrl.setPrimary( true );
				newPrimaryUrl.setHttpStatus( HttpStatus.OK );
			}

			if ( existing == null || !existing.isPrimary() ) {
				endpoint.getPrimaryUrl().ifPresent(
						currentPrimaryUrl -> {
							currentPrimaryUrl.setPrimary( false );
							currentPrimaryUrl.setHttpStatus( HttpStatus.MOVED_PERMANENTLY );
							urlRepository.save( currentPrimaryUrl );
						}
				);

				urlRepository.save( newPrimaryUrl );
			}
		}
	}

	private String generateUrl( WebCmsArticle article ) {
		WebCmsPage articleTemplatePage = article.getPublication().getArticleTemplatePage();

		if ( articleTemplatePage != null ) {
			return WebCmsUtils.combineUrlSegments( articleTemplatePage.getCanonicalPath(), WebCmsUtils.generateUrlPathSegment( article.getTitle() ) );
		}

		return "/" + WebCmsUtils.generateUrlPathSegment( article.getTitle() );
	}
}
