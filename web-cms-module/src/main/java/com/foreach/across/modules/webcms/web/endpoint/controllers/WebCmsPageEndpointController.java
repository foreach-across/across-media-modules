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

package com.foreach.across.modules.webcms.web.endpoint.controllers;

import com.foreach.across.modules.webcms.domain.page.WebCmsPageEndpoint;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Controller
@WebCmsEndpointMapping(WebCmsPageEndpoint.class)
public class WebCmsPageEndpointController
{
	private WebCmsPageService pageService;

	@WebCmsEndpointMapping(status = HttpStatus.OK)
	public void render( WebCmsUrl url, WebCmsPageEndpoint endpoint, ModelMap model ) {
		model.addAttribute( "page", endpoint.getPage() );
		model.addAttribute( "contentSections", pageService.retrieveContentSections( endpoint.getPage() ) );
	}

	@WebCmsEndpointMapping(series = HttpStatus.Series.REDIRECTION)
	public RedirectView redirect( WebCmsUrl url, WebCmsPageEndpoint endpoint ) {
		return endpoint.getPrimaryUrl().map( primary -> {
			RedirectView result = new RedirectView( primary.getPath() );
			result.setStatusCode( url.getHttpStatus() );
			return result;
		} ).orElseGet( () -> {
			RedirectView result = new RedirectView( "/404" );
			result.setStatusCode( HttpStatus.NOT_FOUND );
			return result;
		} );
	}

	@Autowired
	protected void setPageService( WebCmsPageService pageService ) {
		this.pageService = pageService;
	}
}
