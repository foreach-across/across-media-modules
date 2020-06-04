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

package com.foreach.across.modules.webcms.domain.page.services;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierService;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
@Slf4j
@RequiredArgsConstructor
class WebCmsPageServiceImpl implements WebCmsPageService
{
	private final PagePropertyGenerator pagePropertyGenerator;
	private final WebCmsPageRepository pageRepository;
	private final WebCmsMultiDomainService multiDomainService;
	private final WebCmsTypeSpecifierService typeSpecifierService;

	@Override
	public WebCmsPageType getPageType( String objectId ) {
		return typeSpecifierService.getTypeSpecifier( objectId, WebCmsPageType.class );
	}

	@Override
	public WebCmsPageType getPageTypeByKey( String typeKey ) {
		return typeSpecifierService.getTypeSpecifierByKey( typeKey, WebCmsPageType.class );
	}

	@Override
	public WebCmsPageType getPageTypeByKey( String typeKey, WebCmsDomain domain ) {
		return typeSpecifierService.getTypeSpecifierByKey( typeKey, WebCmsPageType.class, domain );
	}

	@Override
	public Optional<WebCmsPage> findByCanonicalPath( String canonicalPath ) {
		return findByCanonicalPathAndDomain( canonicalPath, multiDomainService.getCurrentDomainForType( WebCmsPage.class ) );
	}

	@Override
	public Optional<WebCmsPage> findByCanonicalPathAndDomain( String canonicalPath, WebCmsDomain domain ) {
		Optional<WebCmsPage> page = pageRepository.findOneByCanonicalPathAndDomain( canonicalPath, domain );

		if ( !page.isPresent() && !WebCmsDomain.isNoDomain( domain ) && multiDomainService.isNoDomainAllowed( WebCmsPage.class ) ) {
			page = pageRepository.findOneByCanonicalPathAndDomain( canonicalPath, WebCmsDomain.NONE );
		}

		return page;
	}

	@Override
	public Map<ModificationType, ModificationReport<PrepareModificationType, Object>> prepareForSaving( WebCmsPage page ) {
		return pagePropertyGenerator.prepareForSaving( page );
	}
}
