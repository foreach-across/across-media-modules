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
import com.foreach.across.modules.webcms.domain.endpoint.web.IgnoreEndpointModel;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointModelLoader;
import com.foreach.across.modules.webcms.domain.endpoint.web.interceptor.WebCmsEndpointHandlerInterceptor;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 * Contains default handler methods for dealing with {@link WebCmsAssetEndpoint}.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Controller
@Slf4j
class WebCmsAssetEndpointController
{
	/**
	 * Default handler method for any asset endpoint.
	 * Does not actually do anything except ensuring that a handler method is found for the asset.
	 * The available default model has been determined by {@link WebCmsEndpointModelLoader} beans.
	 * <p/>
	 * If no {@link WebCmsEndpointHandlerInterceptor#DEFAULT_TEMPLATE_ATTRIBUTE} is configured,
	 * a default view name will be generated based on {@link WebCmsAsset#getObjectType()}.
	 */
	@WebCmsAssetMapping
	public String renderAsset( HttpServletRequest request, WebCmsAsset asset ) {
		LOG.trace( "Using default asset rendering handler method for {}", asset );
		String defaultViewName = (String) request.getAttribute( WebCmsEndpointHandlerInterceptor.DEFAULT_TEMPLATE_ATTRIBUTE );
		return defaultViewName != null ? defaultViewName : "asset/" + asset.getObjectType();
	}

	@IgnoreEndpointModel
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