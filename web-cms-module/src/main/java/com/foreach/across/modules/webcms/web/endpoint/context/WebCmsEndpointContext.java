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

package com.foreach.across.modules.webcms.web.endpoint.context;

import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;

/**
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
public interface WebCmsEndpointContext
{
	WebCmsUrl getUrl();

	WebCmsEndpoint getEndpoint();

	<T extends WebCmsEndpoint> T getEndpoint( Class<T> endpointType );

	/**
	 * Returns true if and only if this context is resolved and a url can be found.
	 */
	boolean isAvailable();
}
