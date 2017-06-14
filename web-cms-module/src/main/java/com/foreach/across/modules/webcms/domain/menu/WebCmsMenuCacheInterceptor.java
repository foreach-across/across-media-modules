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

package com.foreach.across.modules.webcms.domain.menu;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Clears the entire {@link WebCmsMenuCache} if a related entity gets updated.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
class WebCmsMenuCacheInterceptor extends EntityInterceptorAdapter<Object>
{
	private final WebCmsMenuCache menuCache;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsMenu.class.isAssignableFrom( entityClass )
				|| WebCmsMenuItem.class.isAssignableFrom( entityClass )
				|| WebCmsEndpoint.class.isAssignableFrom( entityClass )
				|| WebCmsUrl.class.isAssignableFrom( entityClass )
				|| WebCmsAsset.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterDelete( Object entity ) {
		flushRelatedMenuItems( entity );
	}

	@Override
	public void afterCreate( Object entity ) {
		flushRelatedMenuItems( entity );
	}

	@Override
	public void afterUpdate( Object entity ) {
		flushRelatedMenuItems( entity );
	}

	@Override
	public void afterDeleteAll( Class<?> entityClass ) {
		menuCache.clear();
	}

	private void flushRelatedMenuItems( Object entity ) {
		if ( entity instanceof WebCmsMenu ) {
			menuCache.clear();
		}
		else if ( entity instanceof WebCmsMenuItem ) {
			menuCache.remove( ( (WebCmsMenuItem) entity ).getMenu().getName() );
		}
		else {
			// todo: only clear those menus that have an endpoint
			menuCache.clear();
		}
	}
}
