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
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsUrlInterceptor extends EntityInterceptorAdapter<WebCmsUrl>
{
	private final WebCmsUrlRepository urlRepository;
	private final CacheManager cacheManager;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsUrl.class.isAssignableFrom( entityClass );
	}

	@Override
	public void beforeUpdate( WebCmsUrl updatedUrl ) {
		WebCmsUrl current = urlRepository.findOne( updatedUrl.getId() );

		if ( current != null && !StringUtils.equals( current.getPath(), updatedUrl.getPath() ) ) {
			flushFromCache( current );
		}
	}

	@Override
	public void beforeDelete( WebCmsUrl entity ) {
		flushFromCache( entity );
	}

	private void flushFromCache( WebCmsUrl url ) {
		TransactionSynchronizationManager.registerSynchronization( new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit() {
				cacheManager.getCache( "urls" ).evict( url.getPath() );
			}
		} );
	}
}
