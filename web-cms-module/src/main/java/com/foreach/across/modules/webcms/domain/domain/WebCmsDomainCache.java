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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.modules.webcms.WebCmsModuleCache;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.stereotype.Component;

/**
 * Read-through cache for certain domain related objects.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Exposed
@Component
@RequiredArgsConstructor
public class WebCmsDomainCache
{
	private static final Cache DEFAULT_CACHE = new NoOpCache( WebCmsModuleCache.DOMAIN );
	private static final String LOOKUP_DATA_KEY = "lookupData";

	private final CacheManager cacheManager;
	private final WebCmsDomainMetadataFactory metadataFactory;
	private final WebCmsDomainRepository domainRepository;

	private Cache cache = DEFAULT_CACHE;

	public Object getMetadataForDomain( WebCmsDomain domain ) {
		long domainId = WebCmsDomain.isNoDomain( domain ) ? 0 : domain.getId();
		return cache.get( "metadata:" + domainId, () -> metadataFactory.createMetadataForDomain( domain ) );
	}

	public WebCmsDomain getDomain( String objectId ) {
		return cache.get( objectId, () -> {
			WebCmsDomain domain = domainRepository.findOneByObjectId( objectId );
			if ( domain != null ) {
				cache.put( "key:" + domain.getDomainKey(), domain );
			}
			return domain;
		} );
	}

	public WebCmsDomain getDomainByKey( String domainKey ) {
		return cache.get( "key:" + domainKey, () -> {
			WebCmsDomain domain = domainRepository.findOneByDomainKey( domainKey );
			if ( domain != null ) {
				cache.put( domain.getObjectId(), domain );
			}
			return domain;
		} );
	}

	public void put( WebCmsDomain domain ) {
		cache.put( domain.getObjectId(), domain );
		cache.put( "key:" + domain.getDomainKey(), domain );
		cache.evict( "metadata:" + domain.getId() );
		cache.evict( LOOKUP_DATA_KEY );
	}

	public void evict( WebCmsDomain domain ) {
		cache.evict( domain.getObjectId() );
		cache.evict( "key:" + domain.getDomainKey() );
		cache.evict( "metadata:" + domain.getId() );
		cache.evict( LOOKUP_DATA_KEY );
	}

	/**
	 * Store shared lookup data, used by the {@link com.foreach.across.modules.webcms.domain.domain.web.AbstractWebCmsDomainContextFilter}.
	 *
	 * @param lookupData lookup data
	 */
	public void putLookupData( Object lookupData ) {
		cache.put( LOOKUP_DATA_KEY, lookupData );
	}

	/**
	 * Get shared lookup data, used by {@link com.foreach.across.modules.webcms.domain.domain.web.AbstractWebCmsDomainContextFilter}.
	 *
	 * @param expectedType of the lookup data
	 * @return lookup data
	 */
	public <T> T getLookupData( Class<T> expectedType ) {
		return cache.get( LOOKUP_DATA_KEY, expectedType );
	}

	/**
	 * Remove only the shared lookup data from the cache.
	 */
	public void evictLookupData() {
		cache.evict( LOOKUP_DATA_KEY );
	}

	@PostRefresh
	public void reloadCache() {
		Cache candidate = cacheManager.getCache( WebCmsModuleCache.DOMAIN );
		if ( candidate != null ) {
			cache = candidate instanceof TransactionAwareCacheDecorator ? candidate : new TransactionAwareCacheDecorator( candidate );
		}
		else {
			cache = DEFAULT_CACHE;
		}
	}
}
