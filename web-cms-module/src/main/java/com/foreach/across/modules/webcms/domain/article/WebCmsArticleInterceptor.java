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
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.type.WebCmsDefaultComponentsService;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
	private final WebCmsDefaultComponentsService webCmsTypeComponentsStrategy;
	private final WebCmsEndpointService endpointService;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsArticle.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterCreate( WebCmsArticle entity ) {
		createDefaultComponents( entity );
		endpointService.updateOrCreatePrimaryUrlForAsset( generateUrl( entity ), entity, true );
	}

	private void createDefaultComponents( WebCmsArticle entity ) {
		Map<String, String> markerValues = new HashMap<>();
		markerValues.put( "@@title@@", entity.getTitle() );
		markerValues.put( "@@subTitle@@", entity.getSubTitle() );
		markerValues.put( "@@description@@", entity.getDescription() );
		webCmsTypeComponentsStrategy.createDefaultComponents( entity, markerValues );
	}

	@Override
	public void afterUpdate( WebCmsArticle entity ) {
		endpointService.updateOrCreatePrimaryUrlForAsset( generateUrl( entity ), entity, true );
	}

	private String generateUrl( WebCmsArticle article ) {
		WebCmsPage articleTemplatePage = article.getPublication().getArticleTemplatePage();

		if ( articleTemplatePage != null ) {
			return WebCmsUtils.combineUrlSegments( articleTemplatePage.getCanonicalPath(), WebCmsUtils.generateUrlPathSegment( article.getTitle() ) );
		}

		return "/" + WebCmsUtils.generateUrlPathSegment( article.getTitle() );
	}
}
