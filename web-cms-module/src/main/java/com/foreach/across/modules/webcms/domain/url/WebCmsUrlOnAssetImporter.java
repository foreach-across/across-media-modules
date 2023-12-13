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
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.support.EndpointModificationType;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
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
	private final WebCmsEndpointService endpointService;

	@Override
	public boolean supports( Phase phase, WebCmsDataEntry dataEntry, Object asset, WebCmsDataAction action ) {
		return Phase.AFTER_ASSET_SAVED.equals( phase ) && PROPERTY_NAME.equals( dataEntry.getParentKey() ) && asset instanceof WebCmsAsset;
	}

	@Override
	protected WebCmsUrl getExisting( WebCmsDataEntry data, WebCmsAsset parent ) {
		String path = data.getMapData().containsKey( "path" ) ? (String) data.getMapData().get( "path" ) : data.getKey();
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( parent, multiDomainService.getCurrentDomainForEntity( parent ) )
		                                                      .orElse( null );
		if ( endpoint != null ) {
			return endpoint.getUrlWithPath( path ).orElse( null );
		}
		return null;
	}

	@Override
	protected WebCmsUrl createDto( WebCmsDataEntry data, WebCmsUrl existing, WebCmsDataAction action, WebCmsAsset asset ) {
		WebCmsEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( asset, multiDomainService.getCurrentDomainForEntity( asset ) )
		                                                 .orElse( null );
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
		WebCmsEndpoint endpoint = endpointToUse != null
				? endpointToUse
				: assetEndpointRepository.findOneByAssetAndDomain( asset, asset.getDomain() ).orElse( null );
		if ( endpoint != null ) {
			final Map<String, Object> dataValues = data.getMapData();
			String path = dataValues.containsKey( "path" ) ? (String) dataValues.get( "path" ) : data.getKey();
			Object rawStatus = data.isSingleValue() ? data.getSingleValue() : dataValues.get( "httpStatus" );
			HttpStatus httpStatus = rawStatus != null ? HttpStatus.valueOf( (Integer) rawStatus ) : null;

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
	protected void save( WebCmsUrl dto, WebCmsAsset parent ) {
		if ( dto.isPrimary() ) {
			ModificationReport<EndpointModificationType, WebCmsUrl> modificationReport
					= endpointService.updateOrCreatePrimaryUrlForAsset( dto.getPath(), parent, false );

			switch ( modificationReport.getModificationStatus() ) {
				case SUCCESSFUL:
					if ( dto.isPrimaryLocked() ) {
						WebCmsUrl primaryUrl = modificationReport.getNewValue().toDto();
						primaryUrl.setPrimaryLocked( true );
						urlRepository.save( primaryUrl );
					}
					break;
				case FAILED:
					LOG.error( "Unable to change primary url for asset {} to {}", parent, dto.getPath() );
			}
		}
		else {
			urlRepository.save( dto );
		}
	}

	@Override
	protected void delete( WebCmsUrl dto, WebCmsAsset parent ) {
		urlRepository.delete( dto );
	}

	@Override
	protected boolean applyDataValues( Map<String, Object> values, WebCmsUrl dto ) {
		Map<String, Object> filtered = new HashMap<>( values );
		filtered.remove( "httpStatus" );
		boolean modified = super.applyDataValues( filtered, dto );

		if ( dto.isPrimary() ) {
			if ( !values.containsKey( "httpStatus" ) && !HttpStatus.OK.equals( dto.getHttpStatus() ) ) {
				dto.setHttpStatus( HttpStatus.OK );
				modified = true;
			}
			if ( !values.containsKey( "primaryLocked" ) && !dto.isPrimaryLocked() ) {
				dto.setPrimaryLocked( true );
				modified = true;
			}
		}

		return modified;
	}
}
