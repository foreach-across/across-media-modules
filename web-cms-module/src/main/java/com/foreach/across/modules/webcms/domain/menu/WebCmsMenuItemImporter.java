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
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetRepository;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Creates one (or many) @{@link WebCmsMenuItem}s from a import data.
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
	private final WebCmsAssetRepository webCmsAssetRepository;
	private final WebCmsAssetEndpointRepository webCmsAssetEndpointRepository;
	private final WebCmsPageRepository webCmsPageRepository;

	@Override
	public Phase getPhase() {
		return Phase.AFTER_ASSET_SAVED;
	}

	@Override
	public boolean supports( WebCmsDataEntry parentData, String propertyName, Object asset, WebCmsDataAction action ) {
		return PROPERTY_NAME.equals( propertyName ) && asset instanceof WebCmsMenu;
	}

	@Override
	protected WebCmsMenuItem createDto( WebCmsDataEntry menuDataSet, WebCmsMenuItem existing, WebCmsDataAction action, WebCmsMenu parent ) {
		if ( existing != null ) {
			if ( action == WebCmsDataAction.REPLACE ) {
				WebCmsMenuItem menuItem = createNewMenuItemDto( menuDataSet, parent );
				menuItem.setId( existing.getId() );
				attachAsset( menuDataSet, menuItem );
				return menuItem;
			}
			attachAsset( menuDataSet, existing );
			return existing.toDto();
		}
		else {
			WebCmsMenuItem dto = createNewMenuItemDto( menuDataSet, parent );
			attachAsset( menuDataSet, dto );
			return dto;
		}
	}

	private WebCmsMenuItem createNewMenuItemDto( WebCmsDataEntry data, WebCmsMenu parent ) {
		String key = data.getMapData().containsKey( "path" ) ? (String) data.getMapData().get( "path" ) : data.getKey();
		return WebCmsMenuItem.builder().menu( parent ).path( key ).build();
	}

	private void attachAsset( WebCmsDataEntry data, WebCmsMenuItem dto ) {
		String assetKey = (String) data.getMapData().getOrDefault( "asset", "" );

		if ( StringUtils.isNotBlank( assetKey ) ) {
			WebCmsAsset asset =
					assetKey.startsWith( "/" ) ? webCmsPageRepository.findOneByCanonicalPath( assetKey ) : webCmsAssetRepository.findOneByObjectId( assetKey );

			if ( asset != null ) {
				WebCmsEndpoint endpoint = webCmsAssetEndpointRepository.findOneByAsset( asset );
				if ( endpoint != null ) {
					dto.setEndpoint( endpoint );
				}
			}
		}
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
		return webCmsMenuItemRepository.findByMenuNameAndPath( parent.getName(), key );
	}
}