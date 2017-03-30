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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class WebCmsAssetInterceptor extends EntityInterceptorAdapter<WebCmsAsset>
{
	private final WebCmsAssetEndpointRepository endpointRepository;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsAsset.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterCreate( WebCmsAsset entity ) {
		WebCmsAssetEndpoint endpoint = new WebCmsAssetEndpoint();
		endpoint.setAsset( entity );
		endpointRepository.save( endpoint );
	}

	@Override
	public void afterUpdate( WebCmsAsset entity ) {
		WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( entity );

		if ( endpoint == null ) {
			afterCreate( entity );
		}
	}
}
