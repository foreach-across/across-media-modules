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

package com.foreach.across.modules.webcms.domain.page;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Generate primary url when page is being saved.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsPageInterceptor extends EntityInterceptorAdapter<WebCmsPage>
{
	private final WebCmsAssetEndpointRepository endpointRepository;
	private final WebCmsUrlRepository urlRepository;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return WebCmsPage.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterCreate( WebCmsPage entity ) {
		updatePrimaryUrl( entity );
	}

	@Override
	public void afterUpdate( WebCmsPage entity ) {
		updatePrimaryUrl( entity );
	}

	private void updatePrimaryUrl( WebCmsPage page ) {
		if ( page.isPublished() ) {
			WebCmsAssetEndpoint endpoint = endpointRepository.findOneByAsset( page );

			WebCmsUrl newPrimaryUrl = new WebCmsUrl();
			newPrimaryUrl.setPath( page.getCanonicalPath() );
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
				endpoint.getPrimaryUrl().ifPresent(
						currentPrimaryUrl -> {
							currentPrimaryUrl.setPrimary( false );
							currentPrimaryUrl.setHttpStatus( HttpStatus.MOVED_PERMANENTLY );
							urlRepository.save( currentPrimaryUrl );
						}
				);

				urlRepository.save( newPrimaryUrl );
			}
		}
	}
}
