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

package com.foreach.across.modules.webcms.domain.domain.web;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor that resolves the {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext} if none is present yet.
 * Will also clear the context when the request finishes.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RequiredArgsConstructor
public class WebCmsDomainContextInterceptor extends HandlerInterceptorAdapter
{
	private final WebCmsDomainContextResolver domainContextResolver;

	/**
	 * Should the domain context always be resolved, even if there is one set already?
	 * Defaults to {@code true}.
	 */
	@Setter
	private boolean alwaysResolveContext = true;

	@Override
	public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception {
		WebCmsDomainContext domainContext = WebCmsDomainContextHolder.getWebCmsDomainContext();

		if ( domainContext == null || alwaysResolveContext ) {
			domainContext = domainContextResolver.resolveDomainContext( request );

			if ( domainContext != null ) {
				WebCmsDomainContextHolder.setWebCmsDomainContext( domainContext );
				domainContextResolver.setDomainContext( request, response, domainContext );
			}
		}

		return true;
	}

	@Override
	public void afterCompletion( HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex ) throws Exception {
		WebCmsDomainContextHolder.clearWebCmsDomainContext();
	}
}
