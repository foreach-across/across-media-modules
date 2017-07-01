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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

/**
 * Responsible for loading the default endpoint data if an endpoint is available and default data loading is required.
 * <p/>
 * Will iterate through all {@link WebCmsEndpointModelLoader} beans in order, until one them returns {@code false}
 * on the call to {@link WebCmsEndpointModelLoader#loadModel(HttpServletRequest, WebCmsEndpointContext, Model)}.
 *
 * @author Arne Vandamme
 * @see WebCmsEndpointModelLoader
 * @see com.foreach.across.modules.webcms.domain.endpoint.web.interceptor.WebCmsEndpointHandlerInterceptor
 * @since 0.0.2
 */
@Exposed
@ControllerAdvice
@RequiredArgsConstructor
public final class WebCmsEndpointControllerAdvice implements Ordered
{
	/**
	 * If the value of the attribute is {@code true} and {@link WebCmsEndpointContext#isAvailable()} returns {@code true},
	 * only then will the {@link WebCmsEndpointModelLoader} beans be called.
	 */
	public static final String LOAD_ENDPOINT_MODEL_ATTRIBUTE = WebCmsEndpointControllerAdvice.class.getName() + ".LOAD_ENDPOINT_MODEL";

	private final WebCmsEndpointContext context;

	private Collection<WebCmsEndpointModelLoader> endpointModelLoaders = Collections.emptyList();

	@Getter
	@Setter
	private int order = Ordered.HIGHEST_PRECEDENCE;

	@ModelAttribute(binding = false)
	public void loadEndpointData( HttpServletRequest request, Model model ) {
		if ( context.isAvailable() && Boolean.TRUE.equals( request.getAttribute( LOAD_ENDPOINT_MODEL_ATTRIBUTE ) ) ) {
			for ( WebCmsEndpointModelLoader loader : endpointModelLoaders ) {
				if ( !loader.loadModel( request, context, model ) ) {
					return;
				}
			}
		}
	}

	@Autowired
	void setEndpointModelLoaders( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsEndpointModelLoader> endpointModelLoaders ) {
		this.endpointModelLoaders = endpointModelLoaders;
	}
}
