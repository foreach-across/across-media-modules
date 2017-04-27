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
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.services.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;

/**
 * Resolves the url into an endpoint and subsequently validates that the endpoint is accessible.
 * The first validator that applies for that endpoint will be used.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Exposed
public class DefaultWebCmsEndpointContextResolver implements WebCmsEndpointContextResolver
{
	private final UrlPathHelper pathHelper = new UrlPathHelper();
	private final WebCmsEndpointService endpointService;

	private Collection<WebCmsEndpointAccessValidator<?>> endpointAccessValidators = Collections.emptyList();

	@Override
	public void resolve( ConfigurableWebCmsEndpointContext context, HttpServletRequest request ) {
		String path = pathHelper.getPathWithinApplication( request );
		context.setResolved( true );
		LOG.trace( "Resolving path for {}", path );
		endpointService.getUrlForPath( path )
		               .ifPresent( url -> resolve( context, url ) );
		LOG.debug( "Context after resolving {}", context );
	}

	private void resolve( ConfigurableWebCmsEndpointContext context, WebCmsUrl url ) {
		WebCmsEndpoint endpoint = url.getEndpoint();
		LOG.trace( "Found {} as endpoint", endpoint );

		if ( validateAccess( endpoint ) ) {
			context.setUrl( url );
			context.setEndpoint( endpoint );
		}
		else {
			LOG.trace( "Not using endpoint {} as the responsible validator vetoed.", endpoint );
		}
	}

	@SuppressWarnings("unchecked")
	private boolean validateAccess( WebCmsEndpoint endpoint ) {
		for ( WebCmsEndpointAccessValidator validator : endpointAccessValidators ) {
			if ( validator.appliesFor( endpoint ) ) {
				return validator.validateAccess( endpoint );
			}
		}

		return true;
	}

	@Autowired
	void setEndpointAccessValidators( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsEndpointAccessValidator<?>> endpointAccessValidators ) {
		this.endpointAccessValidators = endpointAccessValidators;
	}
}
