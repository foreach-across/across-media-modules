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

package com.foreach.across.modules.webcms.domain.asset.web;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelSet;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointModelLoader;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for a {@link com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointModelLoader} that
 * handles a {@link com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
public abstract class AbstractWebCmsAssetModelLoader<T extends WebCmsAsset> implements WebCmsEndpointModelLoader
{
	private WebCmsComponentModelService componentModelService;
	private WebCmsComponentModelHierarchy componentModelHierarchy;

	@SuppressWarnings("unchecked")
	@Override
	public final boolean loadModel( HttpServletRequest request, WebCmsEndpointContext endpointContext, Model model ) {
		if ( endpointContext.isOfType( WebCmsAssetEndpoint.class ) ) {
			WebCmsAsset asset = endpointContext.getEndpoint( WebCmsAssetEndpoint.class ).getAsset();
			if ( supports( asset ) ) {
				return loadModel( request, (T) asset, model );
			}
		}
		return true;
	}

	protected abstract boolean supports( WebCmsAsset<?> asset );

	protected abstract boolean loadModel( HttpServletRequest request, T asset, Model model );

	protected void registerAssetComponentsForScope( WebCmsAsset owner, String scope, boolean isMainAsset ) {
		WebCmsComponentModelSet componentModelSet = componentModelService.buildComponentModelSetForOwner( owner, false );
		componentModelHierarchy.registerComponentsForScope( componentModelSet, scope );
		if ( isMainAsset ) {
			componentModelHierarchy.registerAliasForScope( WebCmsComponentModelHierarchy.ASSET, scope );
		}
	}

	@Autowired
	public void setComponentModelService( WebCmsComponentModelService componentModelService ) {
		this.componentModelService = componentModelService;
	}

	@Autowired
	public void setComponentModelHierarchy( WebCmsComponentModelHierarchy componentModelHierarchy ) {
		this.componentModelHierarchy = componentModelHierarchy;
	}
}
