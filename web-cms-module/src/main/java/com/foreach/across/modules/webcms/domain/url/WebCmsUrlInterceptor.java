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

package com.foreach.across.modules.webcms.domain.url;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointRepository;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Takes care of flushing the {@link WebCmsUrlCache} whenever a url gets updated.
 * Transaction binding is the responsibility of the  {@link WebCmsUrlCache} itself.
 *
 * @author Arne Vandamme
 * @see WebCmsUrlCache
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
class WebCmsUrlInterceptor extends EntityInterceptorAdapter<WebCmsUrl>
{
	private final WebCmsUrlRepository urlRepository;
	private final WebCmsUrlCache urlCache;
	private final WebCmsEndpointRepository endpointRepository;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsUrl.class.isAssignableFrom( entityClass );
	}

	@Override
	public void beforeCreate( WebCmsUrl entity ) {
		urlCache.remove( entity.getPath() );
	}

	@Override
	public void beforeUpdate( WebCmsUrl updatedUrl ) {
		WebCmsUrl current = urlRepository.findOne( updatedUrl.getId() );

		if ( current != null && !StringUtils.equals( current.getPath(), updatedUrl.getPath() ) ) {
			urlCache.remove( current.getPath() );
		}

		urlCache.remove( updatedUrl.getPath() );
	}

	@Override
	public void beforeDelete( WebCmsUrl entity ) {
		urlCache.remove( entity.getPath() );
	}

	@Override
	public void afterDelete( WebCmsUrl entity ) {
		endpointRepository.refresh( entity.getEndpoint() );
	}

	@Override
	public void afterCreate( WebCmsUrl entity ) {
		endpointRepository.refresh( entity.getEndpoint() );
	}

	@Override
	public void afterUpdate( WebCmsUrl entity ) {
		endpointRepository.refresh( entity.getEndpoint() );
	}
}
