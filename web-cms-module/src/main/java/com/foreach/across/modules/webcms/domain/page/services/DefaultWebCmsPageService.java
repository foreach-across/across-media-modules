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
import com.foreach.across.modules.webcms.domain.page.WebCmsPageSection;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageSectionRepository;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
class DefaultWebCmsPageService implements WebCmsPageService
{
	private PagePropertyGenerator pagePropertyGenerator;
	private WebCmsPageRepository pageRepository;
	private WebCmsPageSectionRepository sectionRepository;

	@Override
	public Optional<WebCmsPage> findByCanonicalPath( String canonicalPath ) {
		return Optional.ofNullable( pageRepository.findByCanonicalPath( canonicalPath ) );
	}

	@Override
	public Map<ModificationType, ModificationReport> prepareForSaving( WebCmsPage page ) {
		return pagePropertyGenerator.prepareForSaving( page );
	}

	@Override
	public Map<String, WebCmsPageSection> retrieveContentSections( WebCmsPage page ) {
		return sectionRepository.findAllByPageOrderBySortIndexAscNameAsc( page )
		                        .stream()
		                        .collect( Collectors.toMap(
				                        WebCmsPageSection::getName,
				                        Function.identity(),
				                        ( a, b ) -> {
					                        throw new IllegalStateException(
							                        "Found multiple sections with same name." );
				                        }, LinkedHashMap::new
		                        ) );
	}

	@Autowired
	public void setPageRepository( WebCmsPageRepository pageRepository ) {
		this.pageRepository = pageRepository;
	}

	@Autowired
	public void setPagePropertyGenerator( PagePropertyGenerator pagePropertyGenerator ) {
		this.pagePropertyGenerator = pagePropertyGenerator;
	}

	@Autowired
	public void setSectionRepository( WebCmsPageSectionRepository sectionRepository ) {
		this.sectionRepository = sectionRepository;
	}
}
