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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationRepository;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ITMultiDomainPublicationReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPublicationRepository publicationRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Test
	void musicNlPublicationShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" ).orElse( null );
		WebCmsPublication publication = publicationRepository.findOneByPublicationKeyAndDomain( "music", domain ).orElse( null );
		assertNull( publication );
	}

	@Test
	void musicBePublicationShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		WebCmsPublication publication = publicationRepository.findOneByPublicationKeyAndDomain( "music", domain ).orElse( null );
		assertNotNull( publication );
		assertEquals( "wcm:asset:publication:music-be", publication.getObjectId() );
		assertEquals( "Music (BE)", publication.getName() );
		assertEquals( pageRepository.findOneByCanonicalPathAndDomain( "/facts/*", domain ), Optional.of( publication.getArticleTemplatePage() ) );
	}

	@Test
	void musicDePublicationShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" ).orElse( null );
		WebCmsPublication publication = publicationRepository.findOneByPublicationKeyAndDomain( "music", domain ).orElse( null );
		assertNotNull( publication );
		assertEquals( "wcm:asset:publication:music-de", publication.getObjectId() );
		assertEquals( "Music (DE)", publication.getName() );
		assertEquals( pageRepository.findOneByCanonicalPathAndDomain( "/facts/*", null ), Optional.of( publication.getArticleTemplatePage() ) );
	}

	@Test
	void moviesNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" ).orElse( null );
		WebCmsPublication publication = publicationRepository.findOneByPublicationKeyAndDomain( "movies", domain ).orElse( null );
		assertNotNull( publication );
		assertEquals( "Movies (NL)", publication.getName() );
		assertEquals( pageRepository.findOneByCanonicalPathAndDomain( "/facts/*", domain ), Optional.of( publication.getArticleTemplatePage() ) );
	}

	@Test
	void moviesDeShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" ).orElse( null );
		WebCmsPublication publication = publicationRepository.findOneByPublicationKeyAndDomain( "movies", domain ).orElse( null );
		assertNotNull( publication );
		assertEquals( "Movies (DE)", publication.getName() );
		assertEquals( pageRepository.findOneByCanonicalPathAndDomain( "/facts/*", null ), Optional.of( publication.getArticleTemplatePage() ) );
	}

	@Test
	void moviesBeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		WebCmsPublication publication = publicationRepository.findOneByPublicationKeyAndDomain( "movies", domain ).orElse( null );
		assertNull( publication );
	}
}
