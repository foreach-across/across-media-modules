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

package com.foreach.across.modules.webcms.domain.page.web;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.web.AbstractWebCmsAssetModelLoader;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelSet;
import com.foreach.across.modules.webcms.domain.endpoint.web.interceptor.WebCmsEndpointHandlerInterceptor;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

import static com.foreach.across.modules.webcms.domain.asset.web.WebCmsAssetModelLoader.ASSET_MODEL_ATTRIBUTE;

/**
 * Loads the initial model for a {@link WebCmsPage}.  Registers the {@link WebCmsPage} as an attribute on the model.
 * The page will actually be registered twice, once as <strong>asset</strong> attribute name, and once as <strong>page</strong> attribute.
 * <p/>
 * This loader also loads the {@link WebCmsComponentModelSet} as default scope on the {@link WebCmsComponentModelHierarchy}.
 * The scope name is <strong>page</strong>.
 * <p/>
 * The page template will be resolved an used as default view name when rendering the page.
 * <p/>
 * Will not allow a next loader to execute if the asset context contains a {@link WebCmsPage}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class WebCmsPageModelLoader extends AbstractWebCmsAssetModelLoader<WebCmsPage>
{
	private final PageTemplateResolver pageTemplateResolver;

	@Override
	protected boolean supports( WebCmsAsset<?> asset ) {
		return asset instanceof WebCmsPage;
	}

	@Override
	protected boolean loadModel( HttpServletRequest request, WebCmsPage page, Model model ) {
		model.addAttribute( ASSET_MODEL_ATTRIBUTE, page );
		model.addAttribute( page.getObjectType(), page );

		registerAssetComponentsForScope( page, page.getObjectType() );

		registerDefaultTemplate( request, page );

		return false;
	}

	private void registerDefaultTemplate( HttpServletRequest request, WebCmsPage page ) {
		request.setAttribute( WebCmsEndpointHandlerInterceptor.DEFAULT_TEMPLATE_ATTRIBUTE, pageTemplateResolver.resolvePageTemplate( page ) );
	}
}
