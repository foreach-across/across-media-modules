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

import com.foreach.across.modules.webcms.data.AbstractWebCmsPropertyDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Creates one (or many) {@link WebCmsMenuItem}s attached to a {@link WebCmsAsset} from a data import.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebCmsMenuItemOnAssetImporter extends AbstractWebCmsPropertyDataImporter<WebCmsAsset, WebCmsMenuItem>
{
	private static final String PROPERTY_NAME = "wcm:menu-items";

	private final WebCmsMenuRepository menuRepository;
	private final WebCmsMenuItemRepository menuItemRepository;
	private final WebCmsAssetEndpointRepository assetEndpointRepository;

	@Override
	public Phase getPhase() {
		return Phase.AFTER_ASSET_SAVED;
	}

	@Override
	public boolean supports( WebCmsDataEntry parentData, String propertyName, Object asset, WebCmsDataAction action ) {
		return PROPERTY_NAME.equals( propertyName ) && asset instanceof WebCmsAsset;
	}

	@Override
	protected WebCmsMenuItem createDto( WebCmsDataEntry menuDataSet, WebCmsMenuItem existing, WebCmsDataAction action, WebCmsAsset asset ) {
		if ( existing != null ) {
			if ( action == WebCmsDataAction.REPLACE ) {
				WebCmsMenuItem menuItem = createNewMenuItemDto( menuDataSet, asset, existing.getEndpoint() );
				if ( menuItem != null ) {
					menuItem.setId( existing.getId() );
				}
				return menuItem;
			}

			return existing.toDto();
		}
		else {
			return createNewMenuItemDto( menuDataSet, asset, null );
		}
	}

	private WebCmsMenuItem createNewMenuItemDto( WebCmsDataEntry data, WebCmsAsset asset, WebCmsEndpoint endpointToUse ) {
		WebCmsEndpoint endpoint = endpointToUse != null ? endpointToUse : assetEndpointRepository.findOneByAsset( asset );

		if ( endpoint != null ) {
			WebCmsUrl primaryUrl = endpoint.getPrimaryUrl().orElse( null );

			if ( primaryUrl != null ) {
				WebCmsMenu menu = retrieveMenu( data );
				boolean generated = !data.getMapData().containsKey( "title" ) && !data.getMapData().containsKey( "path" );

				return WebCmsMenuItem.builder().menu( menu )
				                     .endpoint( endpoint )
				                     .path( primaryUrl.getPath() )
				                     .title( asset.getName() )
				                     .generated( generated )
				                     .build();
			}
		}

		LOG.warn( "Unable to create menu item as no endpoint available for asset {} - data: {}", asset.getObjectId(), data );
		return null;
	}

	@Override
	protected void save( WebCmsMenuItem dto ) {
		menuItemRepository.save( dto );
	}

	@Override
	protected void delete( WebCmsMenuItem dto ) {
		menuItemRepository.delete( dto );
	}

	@Override
	protected WebCmsMenuItem getExisting( WebCmsDataEntry data, WebCmsAsset parent ) {
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAsset( parent );

		if ( endpoint != null ) {
			WebCmsMenu menu = retrieveMenu( data );

			return menuItemRepository.findAllByEndpoint( endpoint )
			                         .stream()
			                         .filter( item -> item.getMenu().equals( menu ) )
			                         .findFirst()
			                         .orElse( null );
		}

		return null;
	}

	private WebCmsMenu retrieveMenu( WebCmsDataEntry data ) {
		return menuRepository.findOneByName( StringUtils.defaultString( (String) data.getMapData().get( "menu" ), data.getKey() ) );
	}
}
