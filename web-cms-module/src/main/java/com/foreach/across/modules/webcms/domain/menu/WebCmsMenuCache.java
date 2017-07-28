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

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.webcms.WebCmsModuleCache;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Read-through cache of menu items.
 * Also listens to updates of related entities to determine if menu needs flushing.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public final class WebCmsMenuCache
{
	public static final String ENDPOINT_ID = "endpointId";
	public static final String ASSET_OBJECT_ID = "assetObjectId";

	private static final Cache DEFAULT_CACHE = new NoOpCache( WebCmsModuleCache.MENU );

	private final CacheManager cacheManager;
	private final WebCmsMenuItemRepository menuItemRepository;

	private Cache cache = DEFAULT_CACHE;

	/**
	 * Returns the flat list of menu items that are registered for a particular menu.
	 *
	 * @param menuName name of he menu
	 * @return collection of items (never {@code null})
	 */
	@SuppressWarnings("unchecked")
	public Collection<Menu> getMenuItems( String menuName ) {
		Collection<Menu> items = cache.get( menuName, Collection.class );

		if ( items == null ) {
			items = menuItemRepository.findAllByMenuName( menuName )
			                          .stream()
			                          .map( menuItem -> {
				                          Menu item = new Menu( menuItem.getPath(), menuItem.getTitle() );
				                          item.setOrder( menuItem.getSortIndex() );
				                          item.setGroup( menuItem.isGroup() );
				                          item.setDisabled( !isMenuItemAvailable( menuItem ) );

				                          item.setUrl( menuItem.getUrl() );

				                          WebCmsEndpoint endpoint = menuItem.getEndpoint();

				                          if ( !item.hasUrl() && endpoint != null ) {
					                          endpoint.getPrimaryUrl().ifPresent( url -> item.setUrl( url.getPath() ) );
				                          }

				                          if ( endpoint != null ) {
					                          item.setAttribute( ENDPOINT_ID, menuItem.getEndpoint().getId() );
					                          if ( endpoint instanceof WebCmsAssetEndpoint ) {
						                          item.setAttribute( ASSET_OBJECT_ID, ( (WebCmsAssetEndpoint) endpoint ).getAsset().getObjectId() );
					                          }
				                          }

				                          return item;
			                          } )
			                          .collect( Collectors.toList() );

			cache.put( menuName, items );
		}

		return items;
	}

	private boolean isMenuItemAvailable( WebCmsMenuItem menuItem ) {
		if ( menuItem.hasEndpoint() ) {
			WebCmsEndpoint endpoint = menuItem.getEndpoint();
			return !( endpoint instanceof WebCmsAssetEndpoint ) || ( (WebCmsAssetEndpoint) endpoint ).getAsset().isPublished();
		}
		return true;
	}

	public void remove( String menuName ) {
		cache.evict( menuName );
	}

	public void clear() {
		cache.clear();
	}

	@PostRefresh
	public void reloadCache() {
		Cache candidate = cacheManager.getCache( WebCmsModuleCache.MENU );
		cache = candidate != null ? new TransactionAwareCacheDecorator( candidate ) : DEFAULT_CACHE;
	}
}
