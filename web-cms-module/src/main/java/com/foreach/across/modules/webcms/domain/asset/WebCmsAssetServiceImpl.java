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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
@RequiredArgsConstructor
@Service
public class WebCmsAssetServiceImpl implements WebCmsAssetService
{
	private final WebCmsUriComponentsService uriComponentsService;
	private final WebCmsEndpointService endpointService;
	private final WebCmsAssetEndpointRepository assetEndpointRepository;
	private final WebCmsMultiDomainService multiDomainService;

	@Transactional(readOnly = true)
	@Override
	public Optional<String> buildPreviewUrl( WebCmsAsset asset ) {
		Assert.notNull( asset, "asset is required" );
		return buildPreviewUrlOnDomain( asset, multiDomainService.getCurrentDomainForEntity( asset ) );
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<String> buildPreviewUrlOnDomain( WebCmsAsset asset, WebCmsDomain domain ) {
		Assert.notNull( asset, "asset is required" );
		return getUriComponents( asset, domain ).map( UriComponentsBuilder::toUriString );
	}

	private Optional<UriComponentsBuilder> getUriComponents( WebCmsAsset asset, WebCmsDomain domain ) {
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( asset, domain ).orElse( null );
		return uriComponentsService.buildUriComponents( endpoint, domain ).map( uriComponents -> endpointService.appendPreviewCode( endpoint, uriComponents ) );
	}
}
