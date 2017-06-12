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

package com.foreach.across.modules.webcms.domain.endpoint.web;

import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is responsible for resolving a {@code WebCmsEndpointContext} based on a {@code HttpServletRequest}.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
public interface WebCmsEndpointContextResolver
{
	/**
	 * If a {@code WebCmsUrl} can be found on the given request, this will be put on the given context. This lookup is expensive may only be called once per request.
	 * <p>
	 * After resolving, {@link ConfigurableWebCmsEndpointContext#setResolved(boolean) } is set to {@code true}.
	 *
	 * @see WebCmsEndpointService#getUrlForPath(java.lang.String)
	 */
	void resolve( ConfigurableWebCmsEndpointContext context, HttpServletRequest request );
}
