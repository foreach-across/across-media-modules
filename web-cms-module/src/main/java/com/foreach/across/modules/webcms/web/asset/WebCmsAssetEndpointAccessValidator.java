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

package com.foreach.across.modules.webcms.web.asset;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.web.endpoint.WebCmsEndpointAccessValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Allows access to a {@link com.foreach.across.modules.webcms.domain.asset.WebCmsAsset} if it is published
 * and the publication date is not in the future.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebCmsAssetEndpointAccessValidator<T extends WebCmsAsset> implements WebCmsEndpointAccessValidator<WebCmsAssetEndpoint<T>>
{
	@Override
	public boolean appliesFor( WebCmsEndpoint endpoint ) {
		return endpoint instanceof WebCmsAssetEndpoint;
	}

	@Override
	public boolean validateAccess( WebCmsAssetEndpoint<T> endpoint ) {
		return validateAccess( endpoint.getAsset() );
	}

	protected final boolean validateAccess( WebCmsAsset asset ) {
		if ( asset.isPublished() ) {
			if ( asset.getPublicationDate() == null || asset.getPublicationDate().before( new Date() ) ) {
				return true;
			}
			else {
				LOG.trace( "WebCmsAsset {} is published but publication date {} is in the future - denying access", asset, asset.getPublicationDate() );
			}
		}
		else {
			LOG.trace( "WebCmsAsset {} is not published - denying access", asset );
		}

		return false;
	}
}
