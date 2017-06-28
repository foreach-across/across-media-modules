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

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
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
class DefaultWebCmsPageService implements WebCmsPageService
{
	private final PagePropertyGenerator pagePropertyGenerator;
	private final WebCmsPageRepository pageRepository;

	@Override
	public Optional<WebCmsPage> findByCanonicalPath( String canonicalPath ) {
		return Optional.ofNullable( pageRepository.findOneByCanonicalPath( canonicalPath ) );
	}

	@Override
	public Map<ModificationType, ModificationReport<PrepareModificationType, Object>> prepareForSaving( WebCmsPage page ) {

		return pagePropertyGenerator.prepareForSaving( page );
	}

}