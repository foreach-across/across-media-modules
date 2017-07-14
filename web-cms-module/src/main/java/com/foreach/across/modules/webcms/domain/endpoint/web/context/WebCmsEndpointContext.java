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

package com.foreach.across.modules.webcms.domain.endpoint.web.context;

import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;

/**
 * Represents the resolved {@link WebCmsEndpoint} for the current request.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
public interface WebCmsEndpointContext
{
	/**
	 * @return url that was used for resolving the endpoint
	 */
	WebCmsUrl getUrl();

	/**
	 * @return resolved endpoint
	 */
	WebCmsEndpoint getEndpoint();

	/**
	 * @param endpointType to coerce to
	 * @param <T>          endpoint type
	 * @return resolved endpoint as type
	 */
	<T extends WebCmsEndpoint> T getEndpoint( Class<T> endpointType );

	/**
	 * Returns true if and only if this context is resolved and an endpoint was found.
	 */
	boolean isAvailable();

	/**
	 * @return true if the context is rendered in preview mode - in this case {@link #isAvailable()} should also return true
	 */
	boolean isPreviewMode();

	/**
	 * @return true if the context is available and of the expected type
	 */
	<T extends WebCmsEndpoint> boolean isOfType( Class<T> endpointType );
}
