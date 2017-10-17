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

import com.foreach.across.modules.webcms.data.AbstractWebCmsPropertyDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates one or many {@link WebCmsUrl}s linked to a {@link WebCmsAsset} from a data import.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebCmsUrlOnAssetImporter extends AbstractWebCmsPropertyDataImporter<WebCmsAsset, WebCmsUrl>
{
	static final String PROPERTY_NAME = "wcm:urls";

	private final WebCmsUrlRepository urlRepository;
	private final WebCmsAssetEndpointRepository assetEndpointRepository;
	private final WebCmsMultiDomainService multiDomainService;

	@Override
	public Phase getPhase() {
		return Phase.AFTER_ASSET_SAVED;
	}

	@Override
	public boolean supports( WebCmsDataEntry parentData, String propertyName, Object asset, WebCmsDataAction action ) {
		return PROPERTY_NAME.equals( propertyName ) && asset instanceof WebCmsAsset;
	}

	@Override
	protected WebCmsUrl getExisting( WebCmsDataEntry data, WebCmsAsset parent ) {
		String path = data.getMapData().containsKey( "path" ) ? (String) data.getMapData().get( "path" ) : data.getKey();
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( parent, multiDomainService.getCurrentDomainForEntity( parent ) );
		if ( endpoint != null ) {
			return endpoint.getUrlWithPath( path ).orElse( null );
		}
		return null;
	}

	@Override
	protected WebCmsUrl createDto( WebCmsDataEntry data, WebCmsUrl existing, WebCmsDataAction action, WebCmsAsset asset ) {
		WebCmsEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( asset, multiDomainService.getCurrentDomainForEntity( asset ) );
		if ( existing != null ) {
			if ( action == WebCmsDataAction.REPLACE ) {
				WebCmsUrl url = createNewWebCmsUrlDto( data, asset, endpoint );
				if ( url != null ) {
					url.setId( existing.getId() );
				}
				return url;
			}

			return existing.toDto();
		}
		else {
			return createNewWebCmsUrlDto( data, asset, endpoint );
		}
	}

	private WebCmsUrl createNewWebCmsUrlDto( WebCmsDataEntry data, WebCmsAsset asset, WebCmsEndpoint endpointToUse ) {
		WebCmsEndpoint endpoint = endpointToUse != null ? endpointToUse : assetEndpointRepository.findOneByAssetAndDomain( asset, asset.getDomain() );
		if ( endpoint != null ) {
			String path = data.getMapData().containsKey( "path" ) ? (String) data.getMapData().get( "path" ) : data.getKey();
			HttpStatus httpStatus = data.getMapData().containsKey( "httpStatus" )
					? HttpStatus.valueOf( (Integer) data.getMapData().get( "httpStatus" ) )
					: HttpStatus.MOVED_PERMANENTLY;
			return WebCmsUrl.builder()
			                .path( path )
			                .httpStatus( httpStatus )
			                .endpoint( endpoint )
			                .build();
		}
		LOG.warn( "Unable to create url as no endpoint available for asset {} - data: {}", asset.getObjectId(), data );
		return null;
	}

	@Override
	protected void save( WebCmsUrl dto ) {
		if ( dto.isPrimary() ) {
			dto.getEndpoint().getUrls().stream()
			   .filter( WebCmsUrl::isPrimary )
			   .forEach( url -> {
				   url.setPrimary( false );
				   urlRepository.save( url );
			   } );
		}
		urlRepository.save( dto );
	}

	@Override
	protected void delete( WebCmsUrl dto ) {
		urlRepository.delete( dto );
	}

	@Override
	protected boolean applyDataValues( Map<String, Object> values, WebCmsUrl dto ) {
		Map<String, Object> filtered = new HashMap<>( values );
		filtered.remove( "httpStatus" );
		return super.applyDataValues( filtered, dto );
	}
}
