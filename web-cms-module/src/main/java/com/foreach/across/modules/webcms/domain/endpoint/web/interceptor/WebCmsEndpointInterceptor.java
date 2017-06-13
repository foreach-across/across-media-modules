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

package com.foreach.across.modules.webcms.domain.endpoint.web.interceptor;

import com.foreach.across.core.annotations.Event;
import com.foreach.across.modules.bootstrapui.resource.JQueryWebResources;
import com.foreach.across.modules.web.events.BuildTemplateWebResourcesEvent;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interceptor is responsible for putting the status code of the resolved {@link WebCmsEndpointContext} on the {@link HttpServletResponse}.
 * Also registers
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@RequiredArgsConstructor
public class WebCmsEndpointInterceptor extends HandlerInterceptorAdapter
{
	private final WebCmsEndpointContext context;

	@Override
	public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception {
		if ( context.isAvailable() ) {
			response.setStatus( context.getUrl().getHttpStatus().value() );

			if ( context.isPreviewMode() ) {
				response.setHeader( "X-WCM-Preview", "true" );
			}
		}

		return true;
	}

	@Event
	void registerPreviewModeWebResources( BuildTemplateWebResourcesEvent webResourcesEvent ) {
		if ( context.isAvailable() && context.isPreviewMode() ) {
			webResourcesEvent.addPackage( JQueryWebResources.NAME );
			webResourcesEvent.addWithKey( WebResource.CSS, WebCmsModule.NAME + "-inline-editor", "/static/WebCmsModule/css/wcm-inline-editor.css",
			                              WebResource.VIEWS );
			webResourcesEvent.addWithKey( WebResource.JAVASCRIPT_PAGE_END, WebCmsModule.NAME + "-preview", "/static/WebCmsModule/js/wcm-preview-mode.js",
			                              WebResource.VIEWS );
		}
	}
}
