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

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.web.PageTemplateResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interceptor is responsible for putting the template of {@link WebCmsEndpointContext} on the {@link ModelAndView} if
 * it was not yet set.  It is important that this interceptor is handled before the {@link com.foreach.across.modules.web.template.WebTemplateInterceptor}
 * because the view of the {@link WebCmsPage} in the context must be set before applying the layout.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Slf4j
public class WebCmsPageEndpointInterceptor extends HandlerInterceptorAdapter
{
	private final WebCmsEndpointContext context;
	private final PageTemplateResolver templateResolver;

	/**
	 * If the incoming {@link ModelAndView} has a view name which differs from the template of the page on the {@link WebCmsEndpointContext}, we
	 * override the view name with the latter.
	 * <p>
	 * Because the {@link org.springframework.web.servlet.DispatcherServlet} always puts a default view name before handling the interceptors, we
	 * can not just do a `null`check on the view name.
	 *
	 * @see DispatcherServlet#applyDefaultViewName(javax.servlet.http.HttpServletRequest, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle( HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView ) throws Exception {
		try {
			WebCmsEndpoint endpoint = context.getEndpoint();

			if ( endpoint instanceof WebCmsAssetEndpoint ) {
				WebCmsAsset asset = ( (WebCmsAssetEndpoint) endpoint ).getAsset();

				// todo: generic template resolving for WebCmsAsset
				if ( asset instanceof WebCmsPage ) {
					String resolvedTemplate = templateResolver.resolvePageTemplate( (WebCmsPage) asset );
					if ( modelAndView != null && modelAndView.getViewName() != null && modelAndView.isReference() &&
							!modelAndView.getViewName().equals( resolvedTemplate ) ) {
						LOG.trace( "Current model has an incorrect view {}, setting page template {} instead", modelAndView.getViewName(), resolvedTemplate );
						modelAndView.setViewName( resolvedTemplate );
					}
				}
			}
		}
		catch ( ClassCastException cce ) {
			LOG.trace( "Given context {} is not for a WebCmsAssetEndpoint", context, cce );
		}
		catch ( Exception e ) {
			LOG.warn( "Something went wrong while checking the view name with the configured template", e );
		}
	}
}
