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

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelSet;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Controller
@RequiredArgsConstructor
public class WebCmsAssetEndpointController
{
	private final WebCmsComponentModelHierarchy componentModelHierarchy;
	private final WebCmsComponentModelService componentModelService;

	@WebCmsAssetMapping
	public void render( WebCmsUrl url, WebCmsAssetEndpoint endpoint, ModelMap model ) {
		model.addAttribute( "asset", endpoint.getAsset() );

		WebCmsComponentModelSet componentModelSet = componentModelService.buildComponentModelSetForOwner( endpoint.getAsset() );
		componentModelHierarchy.registerComponentsForScope( componentModelSet, "asset" );

		//model.addAttribute( "webCmsComponent", componentModelHierarchy );
		/*model.addAttribute( "componentHierarchy", componentModelHierarchy );
		model.addAttribute( "components", componentModelSet );*/
	}

	@WebCmsAssetMapping(series = HttpStatus.Series.REDIRECTION)
	public RedirectView redirect( WebCmsUrl url, WebCmsAssetEndpoint endpoint ) {
		return endpoint.getPrimaryUrl()
		               .map( primary -> {
			               RedirectView result = new RedirectView( primary.getPath() );
			               result.setStatusCode( url.getHttpStatus() );
			               return result;
		               } )
		               .orElseGet( () -> {
			               RedirectView result = new RedirectView( "/404" );
			               result.setStatusCode( HttpStatus.NOT_FOUND );
			               return result;
		               } );
	}
}