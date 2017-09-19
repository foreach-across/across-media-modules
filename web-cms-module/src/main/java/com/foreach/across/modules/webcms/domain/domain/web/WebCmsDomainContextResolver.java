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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public interface WebCmsDomainContextResolver
{
	/**
	 * Attempts to resolve the domain context attached to the current request.
	 *
	 * @param request to resolve the context for
	 * @return domain context
	 */
	WebCmsDomainContext resolveDomainContext( HttpServletRequest request );

	/**
	 * Set the domain context for the current request.
	 * Depending on the actual implementation this might for example set a cookie or a session variable...
	 * <p/>
	 * If the domain context is {@code null}, this method is the same as removing the context.
	 *
	 * @param request       of the domain
	 * @param response      attached
	 * @param domainContext to set
	 */
	void setDomainContext( HttpServletRequest request, HttpServletResponse response, WebCmsDomainContext domainContext );
}
