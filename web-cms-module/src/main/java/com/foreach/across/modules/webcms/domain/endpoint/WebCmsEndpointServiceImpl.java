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

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.endpoint.support.EndpointModificationType;
import com.foreach.across.modules.webcms.domain.endpoint.support.PrimaryUrlForAssetFailedEvent;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrlCache;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.foreach.across.modules.webcms.domain.endpoint.support.EndpointModificationType.PRIMARY_URL_UPDATED;
import static com.foreach.across.modules.webcms.infrastructure.ModificationStatus.*;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebCmsEndpointServiceImpl implements WebCmsEndpointService
{
	private final WebCmsAssetEndpointRepository endpointRepository;
	private final WebCmsUrlRepository urlRepository;
	private final WebCmsUrlCache urlCache;
	private final AcrossEventPublisher eventPublisher;

	@Override
	public Optional<WebCmsUrl> getUrlForPath( String path ) {
		return urlCache.getUrlForPath( path );
	}

	@Transactional
	@Override
	public ModificationReport<EndpointModificationType, WebCmsUrl> updateOrCreatePrimaryUrlForAsset( String primaryUrl,
	                                                                                                 WebCmsAsset asset,
	                                                                                                 boolean publishEventOnFailure ) {
		boolean canBeUpdated = asset.getPublicationDate() == null || ( new Date() ).before( asset.getPublicationDate() );

		WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( asset );

		if ( endpoint != null ) {
			Optional<WebCmsUrl> currentUrl = endpoint.getPrimaryUrl();

			if ( currentUrl.isPresent() && currentUrl.get().isPrimaryLocked() && currentUrl.get().isPrimary() ) {
				return new ModificationReport<>( PRIMARY_URL_UPDATED, SKIPPED, currentUrl.get(), null );
			}

			WebCmsUrl newPrimaryUrl = new WebCmsUrl();
			newPrimaryUrl.setPath( primaryUrl );
			newPrimaryUrl.setHttpStatus( HttpStatus.OK );
			newPrimaryUrl.setPrimary( true );
			newPrimaryUrl.setEndpoint( endpoint );

			WebCmsUrl existing = urlRepository.findOneByPath( primaryUrl );

			if ( existing != null && !endpoint.equals( existing.getEndpoint() ) ) {
				ModificationReport<EndpointModificationType, WebCmsUrl> modificationReport =
						new ModificationReport<>( PRIMARY_URL_UPDATED, FAILED, currentUrl.orElse( null ), newPrimaryUrl );
				LOG.warn( "Unable to update primary URL for {} - another asset already uses path {}", asset, existing.getPath() );

				if ( publishEventOnFailure ) {
					// Allow event handlers to take action
					PrimaryUrlForAssetFailedEvent event = new PrimaryUrlForAssetFailedEvent( asset, endpoint, modificationReport );
					eventPublisher.publish( event );
					modificationReport = event.getModificationReport();
				}

				return modificationReport;
			}

			if ( existing != null && !existing.isPrimary() ) {
				newPrimaryUrl = existing.toDto();
				newPrimaryUrl.setPrimary( true );
				newPrimaryUrl.setHttpStatus( HttpStatus.OK );
			}

			if ( existing == null || !existing.isPrimary() ) {
				AtomicReference<WebCmsUrl> primaryUpdated = new AtomicReference<>();

				currentUrl.ifPresent(
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
					return new ModificationReport<>( PRIMARY_URL_UPDATED, SUCCESSFUL, currentUrl.orElse( null ), newPrimaryUrl );
				}
				else {
					return new ModificationReport<>( PRIMARY_URL_UPDATED, SUCCESSFUL, currentUrl.orElse( null ), primaryUpdated.get() );
				}
			}
		}

		return new ModificationReport<>( PRIMARY_URL_UPDATED, SKIPPED, null, null );
	}

	@Transactional(readOnly = true)
	@Override
	public Optional<WebCmsUrl> getPrimaryUrlForAsset( WebCmsAsset asset ) {
		WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( asset );

		if ( endpoint != null ) {
			return endpoint.getPrimaryUrl();
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
