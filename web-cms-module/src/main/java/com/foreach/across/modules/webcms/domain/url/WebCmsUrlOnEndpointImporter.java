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
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates one or many {@link WebCmsUrl}s linked to a {@link WebCmsEndpoint} from a data import.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@RequiredArgsConstructor
@Component
public class WebCmsUrlOnEndpointImporter extends AbstractWebCmsPropertyDataImporter<WebCmsEndpoint, WebCmsUrl>
{
	private static final String PROPERTY_NAME = WebCmsUrlOnAssetImporter.PROPERTY_NAME;

	private final WebCmsUrlRepository urlRepository;

	@Override
	public boolean supports( Phase phase, WebCmsDataEntry dataEntry, Object asset, WebCmsDataAction action ) {
		return Phase.AFTER_ASSET_SAVED.equals( phase ) && PROPERTY_NAME.equals( dataEntry.getParentKey() ) && asset instanceof WebCmsEndpoint;
	}

	@Override
	protected WebCmsUrl getExisting( WebCmsDataEntry data, WebCmsEndpoint parent ) {
		String path = data.getMapData().containsKey( "path" ) ? (String) data.getMapData().get( "path" ) : data.getKey();
		return parent.getUrlWithPath( path ).orElse( null );
	}

	@Override
	protected WebCmsUrl createDto( WebCmsDataEntry data, WebCmsUrl existing, WebCmsDataAction action, WebCmsEndpoint parent ) {
		if ( existing != null ) {
			if ( action == WebCmsDataAction.REPLACE ) {
				WebCmsUrl url = createNewWebCmsUrlDto( data, parent );
				if ( url != null ) {
					url.setId( existing.getId() );
				}
				return url;
			}

			return existing.toDto();
		}
		else {
			return createNewWebCmsUrlDto( data, parent );
		}
	}

	private WebCmsUrl createNewWebCmsUrlDto( WebCmsDataEntry data, WebCmsEndpoint endpoint ) {
		String path = data.getMapData().containsKey( "path" ) ? (String) data.getMapData().get( "path" ) : data.getKey();
		Object rawStatus = data.isSingleValue() ? data.getSingleValue() : data.getMapData().get( "httpStatus" );

		if ( rawStatus == null ) {
			throw new IllegalArgumentException( "A valid HTTP status is required for importing urls" );
		}

		HttpStatus httpStatus = HttpStatus.valueOf( (Integer) rawStatus );

		return WebCmsUrl.builder()
		                .path( path )
		                .httpStatus( httpStatus )
		                .endpoint( endpoint )
		                .build();

	}

	@Override
	protected void save( WebCmsUrl dto, WebCmsEndpoint parent ) {
		urlRepository.save( dto );
	}

	@Override
	protected void delete( WebCmsUrl dto, WebCmsEndpoint parent ) {
		urlRepository.delete( dto );
	}

	@Override
	protected boolean applyDataValues( Map<String, Object> values, WebCmsUrl dto ) {
		Map<String, Object> filtered = new HashMap<>( values );
		filtered.remove( "httpStatus" );
		return super.applyDataValues( filtered, dto );
	}
}
