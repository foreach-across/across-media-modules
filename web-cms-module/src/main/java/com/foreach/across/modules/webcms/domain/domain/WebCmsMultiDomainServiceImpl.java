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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

/**
 * Default implementation of {@link WebCmsMultiDomainService}.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Service
@RequiredArgsConstructor
class WebCmsMultiDomainServiceImpl implements WebCmsMultiDomainService
{
	private final WebCmsMultiDomainConfiguration multiDomainConfiguration;
	private final WebCmsDomainMetadataFactory metadataFactory;

	@Override
	public WebCmsDomain getCurrentDomain() {
		WebCmsDomainContext domainContext = WebCmsDomainContextHolder.getWebCmsDomainContext();
		return domainContext != null ? domainContext.getDomain() : null;
	}

	@Override
	public <U> U getCurrentDomainMetadata( Class<U> metadataType ) {
		WebCmsDomainContext domainContext = WebCmsDomainContextHolder.getWebCmsDomainContext();
		return domainContext != null ? domainContext.getMetadata( metadataType ) : null;
	}

	@Override
	public <U> U getMetadataForDomain( WebCmsDomain domain, Class<U> metadataType ) {
		return metadataType.cast( metadataFactory.createMetadataForDomain( domain ) );
	}

	@Override
	public WebCmsDomain getCurrentDomainForEntity( Object entity ) {
		return isDomainBound( entity ) ? getCurrentDomain() : WebCmsDomain.NONE;
	}

	@Override
	public WebCmsDomain getCurrentDomainForType( Class<?> entityType ) {
		return isDomainBound( entityType ) ? getCurrentDomain() : WebCmsDomain.NONE;
	}

	@Override
	public boolean isDomainBound( Object entity ) {
		return entity != null && isDomainBound( ClassUtils.getUserClass( entity ) );
	}

	@Override
	public boolean isDomainBound( Class<?> entityType ) {
		return entityType != null && multiDomainConfiguration.isDomainBound( entityType );
	}

	@Override
	public boolean isNoDomainAllowed( Object entity ) {
		return entity != null && isNoDomainAllowed( ClassUtils.getUserClass( entity ) );
	}

	@Override
	public boolean isNoDomainAllowed( Class<?> entityType ) {
		return entityType != null && multiDomainConfiguration.isNoDomainAllowed( entityType );
	}

	@Override
	public CloseableWebCmsDomainContext attachDomainContext( WebCmsDomain domain ) {
		return new CloseableWebCmsDomainContext(
				domain != null
						? new WebCmsDomainContext( domain, getMetadataForDomain( domain, Object.class ) )
						: WebCmsDomainContext.noDomain( getMetadataForDomain( null, Object.class ) )
		);
	}

	@Override
	public boolean isCurrentDomain( WebCmsDomain domain ) {
		WebCmsDomain currentDomain = getCurrentDomain();
		if ( WebCmsDomain.isNoDomain( currentDomain ) ) {
			return domain == WebCmsDomain.NONE;
		}
		return currentDomain.equals( domain );
	}
}
