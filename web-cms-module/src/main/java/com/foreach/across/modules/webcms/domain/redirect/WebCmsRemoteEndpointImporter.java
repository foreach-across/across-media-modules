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

package com.foreach.across.modules.webcms.domain.redirect;

import com.foreach.across.modules.webcms.data.AbstractWebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Creates one or many {@link WebCmsRemoteEndpoint}s from a yaml file.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
public class WebCmsRemoteEndpointImporter extends AbstractWebCmsDataImporter<WebCmsEndpoint, WebCmsEndpoint>
{
	private final WebCmsRemoteEndpointRepository remoteEndpointRepository;
	private final WebCmsEndpointRepository endpointRepository;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return "redirects".equals( data.getParentKey() );
	}

	@Override
	protected WebCmsEndpoint retrieveExistingInstance( WebCmsDataEntry data ) {
		WebCmsDomain domain = retrieveDomainForDataEntry( data, WebCmsRemoteEndpoint.class );
		String targetUrl = data.getMapData().containsKey( "targetUrl" ) ? (String) data.getMapData().get( "targetUrl" ) : data.getKey();
		return remoteEndpointRepository.findOneByTargetUrlAndDomain( targetUrl, domain ).orElse( null );
	}

	@Override
	protected WebCmsEndpoint createDto( WebCmsDataEntry data, WebCmsEndpoint existing, WebCmsDataAction action, Map<String, Object> dataValues ) {
		if ( existing != null ) {
			if ( action == WebCmsDataAction.REPLACE ) {
				WebCmsRemoteEndpoint endpoint = createNewRemoteEndpointDto( data );
				if ( endpoint != null ) {
					endpoint.setId( existing.getId() );
				}
				return endpoint;
			}
			return existing.toDto();
		}
		else {
			return createNewRemoteEndpointDto( data );
		}
	}

	private WebCmsRemoteEndpoint createNewRemoteEndpointDto( WebCmsDataEntry data ) {
		String targetUrl = data.getMapData().containsKey( "targetUrl" ) ? (String) data.getMapData().get( "targetUrl" ) : data.getKey();
		return WebCmsRemoteEndpoint.builder().targetUrl( targetUrl ).build();
	}

	@Override
	protected void deleteInstance( WebCmsEndpoint instance, WebCmsDataEntry data ) {
		endpointRepository.delete( instance );
	}

	@Override
	protected void saveDto( WebCmsEndpoint dto, WebCmsDataAction action, WebCmsDataEntry data ) {
		endpointRepository.save( dto );
	}

}
