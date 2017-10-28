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
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates one (or many) {@link WebCmsMenuItem}s from import data.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebCmsMenuItemImporter extends AbstractWebCmsPropertyDataImporter<WebCmsMenu, WebCmsMenuItem>
{
	static final String PROPERTY_NAME = "items";

	private final WebCmsMenuItemRepository webCmsMenuItemRepository;
	private final WebCmsAssetEndpointRepository webCmsAssetEndpointRepository;
	private final WebCmsDataConversionService webCmsDataConversionService;
	private final WebCmsMultiDomainService multiDomainService;

	@Override
	public boolean supports( Phase phase,
	                         WebCmsDataEntry dataEntry, Object asset,
	                         WebCmsDataAction action ) {
		return Phase.AFTER_ASSET_SAVED.equals( phase ) && PROPERTY_NAME.equals( dataEntry.getParentKey() ) && asset instanceof WebCmsMenu;
	}

	@Override
	protected WebCmsMenuItem createDto( WebCmsDataEntry menuDataSet, WebCmsMenuItem existing, WebCmsDataAction action, WebCmsMenu parent ) {
		if ( existing != null ) {
			if ( action == WebCmsDataAction.REPLACE ) {
				WebCmsMenuItem menuItem = createNewMenuItemDto( menuDataSet, parent );
				menuItem.setId( existing.getId() );
				return menuItem;
			}
			return existing.toDto();
		}
		else {
			return createNewMenuItemDto( menuDataSet, parent );
		}
	}

	@Override
	protected boolean applyDataValues( Map<String, Object> values, WebCmsMenuItem dto ) {
		Map<String, Object> filtered = new HashMap<>( values );
		attachAsset( filtered, dto );
		return super.applyDataValues( filtered, dto );
	}

	private WebCmsMenuItem createNewMenuItemDto( WebCmsDataEntry data, WebCmsMenu parent ) {
		String key = data.getMapData().containsKey( "path" ) ? (String) data.getMapData().get( "path" ) : data.getKey();
		return WebCmsMenuItem.builder().menu( parent ).path( key ).build();
	}

	private void attachAsset( Map<String, Object> data, WebCmsMenuItem dto ) {
		String assetKey = (String) data.getOrDefault( "asset", "" );
		if ( StringUtils.isNotEmpty( assetKey ) ) {
			WebCmsAsset asset = webCmsDataConversionService.convert( assetKey, WebCmsAsset.class );

			if ( asset != null ) {
				WebCmsEndpoint endpoint = webCmsAssetEndpointRepository.findOneByAssetAndDomain( asset, multiDomainService.getCurrentDomainForEntity( asset ) );

				if ( endpoint != null ) {
					WebCmsUrl primaryUrl = endpoint.getPrimaryUrl().orElse( null );

					if ( primaryUrl != null ) {
						boolean generated = !data.containsKey( "title" ) && !data.containsKey( "path" );
						dto.setEndpoint( endpoint );
						dto.setGenerated( generated );
						if ( dto.isNew() && StringUtils.isEmpty( dto.getPath() ) ) {
							dto.setPath( primaryUrl.getPath() );
						}
						dto.setTitle( asset.getName() );
					}
				}
			}
		}
		data.remove( "asset" );
	}

	@Override
	protected void save( WebCmsMenuItem dto ) {
		webCmsMenuItemRepository.save( dto );
	}

	@Override
	protected void delete( WebCmsMenuItem dto ) {
		webCmsMenuItemRepository.delete( dto );
	}

	protected WebCmsMenuItem getExisting( WebCmsDataEntry dataKey, WebCmsMenu parent ) {
		String key = dataKey.getMapData().containsKey( "path" ) ? (String) dataKey.getMapData().get( "path" ) : dataKey.getKey();
		return webCmsMenuItemRepository.findByMenuAndPath( parent, key );
	}
}