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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Getter
@Setter
@ToString
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Exposed
public class DefaultWebCmsEndpointContext implements ConfigurableWebCmsEndpointContext
{
	private WebCmsUrl url;
	private WebCmsEndpoint endpoint;
	private boolean resolved = false;
	private boolean previewMode = false;

	@Override
	public <T extends WebCmsEndpoint> T getEndpoint( Class<T> endpointType ) {
		return endpointType.cast( getEndpoint() );
	}

	@Override
	public boolean isAvailable() {
		return url != null && endpoint != null && resolved;
	}

	@Override
	public <T extends WebCmsEndpoint> boolean isOfType( Class<T> endpointType ) {
		Assert.notNull( endpointType );
		return isAvailable() && endpointType.isInstance( endpoint );
	}
}
