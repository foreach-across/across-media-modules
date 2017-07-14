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

package com.foreach.across.modules.webcms.domain.redirect.web;

import com.foreach.across.modules.webcms.domain.endpoint.web.IgnoreEndpointModel;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsEndpointMapping;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Controller
public class WebCmsRemoteEndpointController
{
	@IgnoreEndpointModel
	@WebCmsEndpointMapping(value = WebCmsRemoteEndpoint.class, status = HttpStatus.I_AM_A_TEAPOT)
	public RedirectView redirectToRemote( WebCmsUrl url, WebCmsRemoteEndpoint endpoint ) {
		RedirectView redirectView = new RedirectView( endpoint.getTargetUrl() );
		redirectView.setStatusCode( url.getHttpStatus() );
		return redirectView;
	}
}
