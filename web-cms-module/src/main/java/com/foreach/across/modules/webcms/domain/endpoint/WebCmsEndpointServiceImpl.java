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
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebCmsEndpointServiceImpl implements WebCmsEndpointService
{
	private final WebCmsAssetEndpointRepository endpointRepository;
	private final WebCmsUrlRepository urlRepository;

	@Override
	public Optional<WebCmsUrl> getUrlForPath( String path ) {
		return Optional.ofNullable( urlRepository.findOneByPath( path ) );
	}

	@Override
	public Optional<WebCmsUrl> updateOrCreatePrimaryUrlForAsset( String primaryUrl, WebCmsAsset asset ) {
		boolean canBeUpdated = asset.getPublicationDate() == null || ( new Date() ).before( asset.getPublicationDate() );

		WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( asset );

		if ( endpoint != null ) {
			WebCmsUrl newPrimaryUrl = new WebCmsUrl();
			newPrimaryUrl.setPath( primaryUrl );
			newPrimaryUrl.setHttpStatus( HttpStatus.OK );
			newPrimaryUrl.setPrimary( true );
			newPrimaryUrl.setEndpoint( endpoint );

			WebCmsUrl existing = endpoint.getUrlWithPath( newPrimaryUrl.getPath() ).orElse( null );

			if ( existing != null && !existing.isPrimary() ) {
				newPrimaryUrl = existing.toDto();
				newPrimaryUrl.setPrimary( true );
				newPrimaryUrl.setHttpStatus( HttpStatus.OK );
			}

			if ( existing == null || !existing.isPrimary() ) {
				AtomicReference<WebCmsUrl> primaryUpdated = new AtomicReference<>();

				endpoint.getPrimaryUrl().ifPresent(
						currentPrimaryUrl -> {
							WebCmsUrl update = currentPrimaryUrl.toDto();
							if ( canBeUpdated ) {
								update.setPath( primaryUrl );
								primaryUpdated.set( update );
							}
							else {
								update.setPrimary( false );
								update.setHttpStatus( HttpStatus.MOVED_PERMANENTLY );
							}
							urlRepository.save( update );
						}
				);

				if ( primaryUpdated.get() == null ) {
					urlRepository.save( newPrimaryUrl );
					return Optional.of( newPrimaryUrl );
				}
				else {
					return Optional.of( primaryUpdated.get() );
				}
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<String> buildPreviewUrl( WebCmsAsset asset ) {
		Assert.notNull( asset );

		WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( asset );

		if ( endpoint != null ) {
			return endpoint.getPrimaryUrl()
			               .map( url ->
					                     UriComponentsBuilder.fromUriString( url.getPath() )
					                                         .queryParam( "wcmPreview", DigestUtils.md5DigestAsHex( endpoint.getId().toString().getBytes() ) )
					                                         .toUriString()
			               );
		}

		return Optional.empty();
	}

	public boolean isPreviewAllowed( WebCmsEndpoint endpoint, String securityCode ) {
		return !StringUtils.isEmpty( securityCode ) && DigestUtils.md5DigestAsHex( endpoint.getId().toString().getBytes() ).equals( securityCode );
	}
}
