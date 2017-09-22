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
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

/**
 * Loads the initial model for a generic {@link WebCmsAsset}.  Registers the {@link WebCmsAsset} as an attribute on the model.
 * The asset will actually be registered twice, once as <strong>asset</strong> attribute name, and once with an attribute name
 * determined by the value of {@link WebCmsAsset#getObjectType()}.
 * <p/>
 * This loader also loads the {@link com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelSet} as
 * default scope on the {@link com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy}.
 * The scope name is the value of {@link WebCmsAsset#getObjectType()}.
 * <p/>
 * Registered as the last loader to execute.  Will not allow a next loader to execute.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Order
@RequiredArgsConstructor
public class WebCmsAssetModelLoader extends AbstractWebCmsAssetModelLoader<WebCmsAsset<?>>
{
	public static final String ASSET_MODEL_ATTRIBUTE = "asset";

	@Override
	protected boolean supports( WebCmsAsset<?> asset ) {
		return true;
	}

	@Override
	protected boolean loadModel( HttpServletRequest request, WebCmsAsset<?> asset, Model model ) {
		model.addAttribute( ASSET_MODEL_ATTRIBUTE, asset );
		model.addAttribute( asset.getObjectType(), asset );

		registerAssetComponentsForScope( asset, asset.getObjectType(), true );

		return false;
	}
}
