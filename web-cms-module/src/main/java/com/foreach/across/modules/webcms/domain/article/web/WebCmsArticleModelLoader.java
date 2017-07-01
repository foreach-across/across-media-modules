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

package com.foreach.across.modules.webcms.domain.article.web;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.web.AbstractWebCmsAssetModelLoader;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelSet;
import com.foreach.across.modules.webcms.domain.endpoint.web.interceptor.WebCmsEndpointHandlerInterceptor;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.web.PageTemplateResolver;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

import static com.foreach.across.modules.webcms.domain.asset.web.WebCmsAssetModelLoader.ASSET_MODEL_ATTRIBUTE;

/**
 * Loads the initial model for a {@link WebCmsArticle}.  Registers the {@link WebCmsArticle} as an attribute on the model.
 * The article will actually be registered twice, once as <strong>asset</strong> attribute name, and once as <strong>article</strong> attribute.
 * <p/>
 * The {@link WebCmsPublication} the article belongs to will be added as <strong>publication</strong> model attribute.
 * The layout page (value of {@link WebCmsPublication#getArticleTemplatePage()} will be added as <strong>page</strong> model attribute.
 * <p/>
 * This loader also loads the article {@link WebCmsComponentModelSet} as default scope on the {@link WebCmsComponentModelHierarchy}.
 * The scope name is <strong>article</strong>.  The components of the layout page will also be registered as <strong>page</strong> scope
 * after the article components.
 * <p/>
 * The layout page will be used to resolve the default template that should be used for the article.
 * <p/>
 * Will not allow a next loader to execute if the asset context contains a {@link WebCmsArticle}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 2)
@RequiredArgsConstructor
public class WebCmsArticleModelLoader extends AbstractWebCmsAssetModelLoader<WebCmsArticle>
{
	private final PageTemplateResolver pageTemplateResolver;

	@Override
	protected boolean supports( WebCmsAsset<?> asset ) {
		return asset instanceof WebCmsArticle;
	}

	@Override
	protected boolean loadModel( HttpServletRequest request, WebCmsArticle article, Model model ) {
		WebCmsPage page = article.getPublication().getArticleTemplatePage();
		if ( page != null ) {
			model.addAttribute( page.getObjectType(), page );
			registerAssetComponentsForScope( page, page.getObjectType() );
			registerDefaultTemplate( request, page );
		}

		model.addAttribute( ASSET_MODEL_ATTRIBUTE, article );
		model.addAttribute( article.getObjectType(), article );
		model.addAttribute( article.getPublication().getObjectType(), article.getPublication() );
		registerAssetComponentsForScope( article, article.getObjectType() );

		return false;
	}

	private void registerDefaultTemplate( HttpServletRequest request, WebCmsPage page ) {
		request.setAttribute( WebCmsEndpointHandlerInterceptor.DEFAULT_TEMPLATE_ATTRIBUTE, pageTemplateResolver.resolvePageTemplate( page ) );
	}
}
