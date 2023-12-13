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

import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.interceptor.WebCmsEndpointHandlerInterceptor;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

/**
 * Beans implementing this interface will be called by the {@link WebCmsEndpointControllerAdvice} if the initial endpoint related
 * model should be loaded.  The latter is determined by the {@link WebCmsEndpointHandlerInterceptor}.
 * <p/>
 * All loader beans will be called in order until one of them returns {@code false} on the call
 * to {@link #loadModel(HttpServletRequest, WebCmsEndpointContext, Model)}.
 * Assigning a correct order to an implementation is crucial.
 *
 * @author Arne Vandamme
 * @see WebCmsEndpointControllerAdvice
 * @see WebCmsEndpointHandlerInterceptor
 * @since 0.0.2
 */
public interface WebCmsEndpointModelLoader
{
	/**
	 * The return value determines if the next loader in the chain should be called.  Once a loader has returned {@code false},
	 * no other loaders will be executed.
	 *
	 * @param request         responsible for loading the context
	 * @param endpointContext loaded - will always return {@code true} on {@link WebCmsEndpointContext#isAvailable()}
	 * @param model           that can be initialized
	 * @return {@code true} if the next loader should still be called, {@code false} if not
	 */
	boolean loadModel( HttpServletRequest request, WebCmsEndpointContext endpointContext, Model model );
}
