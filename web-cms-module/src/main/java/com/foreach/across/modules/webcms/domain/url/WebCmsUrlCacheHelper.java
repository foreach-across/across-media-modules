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

import com.foreach.across.modules.webcms.config.WebCmsModuleCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsUrlCacheHelper
{
	private final CacheManager cacheManager;

	void flushFromCache( WebCmsUrl url ) {
		TransactionSynchronizationManager.registerSynchronization( new EvictCacheTransactionSynchronizer( url.getPath() ) );
	}

	@RequiredArgsConstructor
	private class EvictCacheTransactionSynchronizer extends TransactionSynchronizationAdapter
	{
		private final String url;

		@Override
		public void afterCommit() {
			cacheManager.getCache( WebCmsModuleCache.PATH_TO_URL_CACHE ).evict( url );
		}
	}
}
