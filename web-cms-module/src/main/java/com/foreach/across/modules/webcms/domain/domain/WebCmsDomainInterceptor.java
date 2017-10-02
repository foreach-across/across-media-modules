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

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Takes care of flushing the {@link WebCmsDomainCache}.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
class WebCmsDomainInterceptor extends EntityInterceptorAdapter<WebCmsDomain>
{
	private final WebCmsDomainCache domainCache;

	@Override
	public boolean handles( Class<?> aClass ) {
		return WebCmsDomain.class.isAssignableFrom( aClass );
	}

	@Override
	public void afterDelete( WebCmsDomain entity ) {
		domainCache.evict( entity );
	}

	@Override
	public void afterCreate( WebCmsDomain entity ) {
		domainCache.evictLookupData();
	}

	@Override
	public void afterUpdate( WebCmsDomain entity ) {
		domainCache.evict( entity );
	}
}
