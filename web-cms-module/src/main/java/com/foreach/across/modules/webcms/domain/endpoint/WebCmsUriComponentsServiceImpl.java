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

package com.foreach.across.modules.webcms.domain.endpoint;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
class WebCmsUriComponentsServiceImpl implements WebCmsUriComponentsService
{
	private final WebCmsMultiDomainService multiDomainService;
	private final WebCmsAssetEndpointRepository assetEndpointRepository;

	@Override
	@Transactional(readOnly = true)
	public Optional<UriComponentsBuilder> buildUriComponents( WebCmsAsset asset ) {
		Assert.notNull( asset, "asset is required" );
		return buildUriComponents( asset, resolveDomain( asset ) );
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UriComponentsBuilder> buildUriComponents( WebCmsAsset asset, WebCmsDomain domain ) {
		Assert.notNull( asset, "asset is required" );
		WebCmsEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( asset, domain ).orElse( null );

		if ( endpoint != null ) {
			return buildUriComponents( endpoint, domain );
		}
		return Optional.empty();
	}

	@Override
	public Optional<UriComponentsBuilder> buildUriComponents( WebCmsEndpoint endpoint ) {
		if ( endpoint != null ) {
			return buildUriComponents( endpoint, resolveDomain( endpoint ) );
		}
		return Optional.empty();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UriComponentsBuilder> buildUriComponents( WebCmsEndpoint endpoint, WebCmsDomain domain ) {
		if ( endpoint != null ) {
			return endpoint.getPrimaryUrl().flatMap( url -> buildUriComponents( url, domain ) );
		}
		return Optional.empty();
	}

	@Override
	public Optional<UriComponentsBuilder> buildUriComponents( WebCmsUrl url ) {
		if ( url != null ) {
			return buildUriComponents( url, resolveDomain( url.getEndpoint() ) );
		}
		return Optional.empty();
	}

	@Override
	public Optional<UriComponentsBuilder> buildUriComponents( WebCmsUrl url, WebCmsDomain domain ) {
		if ( url != null ) {
			Object metadataForDomain = multiDomainService.getMetadataForDomain( domain, Object.class );
			WebCmsDomainUrlConfigurer urlConfigurer =
					metadataForDomain instanceof WebCmsDomainUrlConfigurer ? (WebCmsDomainUrlConfigurer) metadataForDomain : null;

			boolean shouldPrefix = ( multiDomainService.isCurrentDomain( domain )
					&& ( urlConfigurer != null && urlConfigurer.isAlwaysPrefix() ) );
			if ( !shouldPrefix ) {
				return Optional.of( UriComponentsBuilder.fromUriString( url.getPath() ) );
			}
			String prefix = StringUtils.defaultString( urlConfigurer.getUrlPrefix(), "" );
			return Optional.of( UriComponentsBuilder.fromUriString( prefix ).path( url.getPath() ) );
		}
		return Optional.empty();
	}

	private WebCmsDomain resolveDomain( WebCmsDomainBound object ) {
		Assert.notNull( object, "object is required" );
		if ( multiDomainService.isDomainBound( object ) ) {
			if ( !WebCmsDomain.isNoDomain( object.getDomain() ) ) {
				return object.getDomain();
			}
			return multiDomainService.getCurrentDomain();
		}
		return WebCmsDomain.NONE;
	}
}
