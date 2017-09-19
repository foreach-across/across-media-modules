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

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.modules.webcms.WebCmsModuleCache;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cache helper for {@link WebCmsUrl} to path mappings.  Note that only the unique {@code Long} id of the
 * {@link WebCmsUrl} will be cached and an additional caching layer (eg. Hibernate 2nd level cache) is advised
 * for caching the actual entities.
 * <p/>
 * This implementation serves as a read-through cache, which will fetch the actual {@link WebCmsUrl} if not found.
 * <p/>
 * The {@link WebCmsUrlCache} always exists but might not use an actual backing {@link org.springframework.cache.Cache}
 * if no cache provider is present.  Uses a {@link TransactionAwareCacheDecorator} around the actual {@link Cache}
 * ensuring that paths are only flushed after a transaction commits in case a running transaction is busy.
 *
 * @author Arne Vandamme
 * @see WebCmsUrlInterceptor
 * @see TransactionAwareCacheDecorator
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public class WebCmsUrlCache
{
	private static final Cache DEFAULT_CACHE = new NoOpCache( WebCmsModuleCache.PATH_TO_URL_ID );

	private final CacheManager cacheManager;
	private final WebCmsUrlRepository urlRepository;

	private Cache cache = DEFAULT_CACHE;

	@SuppressWarnings("unchecked")
	public Optional<WebCmsUrl> getUrlForPathAndDomain( String path, WebCmsDomain domain ) {
		String cacheKey = cacheKey( path, domain );
		Cache.ValueWrapper urlId = cache.get( cacheKey );

		if ( urlId == null ) {
			WebCmsUrl url = urlRepository.findOneByPathAndEndpoint_Domain( path, domain );
			Optional<WebCmsUrl> value = Optional.ofNullable( url );
			cache.put( cacheKey, value.map( WebCmsUrl::getId ).orElse( null ) );
			return value;
		}

		Long actualId = (Long) urlId.get();
		return Optional.ofNullable( actualId != null ? urlRepository.findOne( actualId ) : null );
	}

	public void remove( WebCmsUrl url ) {
		cache.evict( cacheKey( url.getPath(), url.getEndpoint().getDomain() ) );
	}

	private String cacheKey( String path, WebCmsDomain domain ) {
		return ( domain != null ? domain.getId() + ":" : "no-domain:" ) + path;
	}

	@PostRefresh
	public void reloadCache() {
		Cache candidate = cacheManager.getCache( WebCmsModuleCache.PATH_TO_URL_ID );
		cache = candidate != null ? new TransactionAwareCacheDecorator( candidate ) : DEFAULT_CACHE;
	}
}
