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

package it.reference;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeRegistry;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import it.AbstractCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class ITTypeReferenceData extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsTypeSpecifierRepository typeRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsTypeRegistry typeRegistry;

	@Test
	void teaserComponentTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsComponentType.class ).orElse( null );
		WebCmsComponentType teaser = (WebCmsComponentType) typeRepository.findOneByObjectTypeAndTypeKeyAndDomain( typeGroup, "teaser", WebCmsDomain.NONE )
		                                                                 .orElse( null );
		assertNotNull( teaser );
	}

	@Test
	void bigTeaserComponentTypeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "domain.complex.domain" ).orElse( null );
		assertNotNull( domain );

		String typeGroup = typeRegistry.retrieveObjectType( WebCmsComponentType.class ).orElse( null );
		WebCmsComponentType bigTeaser = (WebCmsComponentType) typeRepository.findOneByObjectTypeAndTypeKeyAndDomain(
				typeGroup, "domain.complex.domain:big-teaser", domain
		).orElse( null );
		assertNotNull( bigTeaser );
		assertEquals( domain, bigTeaser.getDomain() );
	}

	@Test
	void smallArticleArticleTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsArticleType.class ).orElse( null );
		WebCmsArticleType smallArticle = (WebCmsArticleType) typeRepository.findOneByObjectTypeAndTypeKeyAndDomain( typeGroup, "small-article",
		                                                                                                            WebCmsDomain.NONE ).orElse( null );
		assertNotNull( smallArticle );
	}

	@Test
	void mediumArticleArticleTypeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "domain.complex.domain" ).orElse( null );
		assertNotNull( domain );

		String typeGroup = typeRegistry.retrieveObjectType( WebCmsArticleType.class ).orElse( null );
		WebCmsArticleType mediumArticle
				= (WebCmsArticleType) typeRepository.findOneByObjectTypeAndTypeKeyAndDomain( typeGroup, "domain.complex.domain:medium-article", domain )
				                                    .orElse( null );
		assertNotNull( mediumArticle );
		assertEquals( domain, mediumArticle.getDomain() );
	}

	@Test
	void funFactsPublicationTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsPublicationType.class ).orElse( null );
		WebCmsPublicationType funFacts = (WebCmsPublicationType) typeRepository.findOneByObjectTypeAndTypeKeyAndDomain( typeGroup, "fun-facts",
		                                                                                                                WebCmsDomain.NONE ).orElse( null );
		assertNotNull( funFacts );
	}

	@Test
	void boringFactsPublicationTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsPublicationType.class ).orElse( null );
		WebCmsPublicationType boringFacts = (WebCmsPublicationType) typeRepository.findOneByObjectTypeAndTypeKeyAndDomain( typeGroup, "boring-facts",
		                                                                                                                   WebCmsDomain.NONE ).orElse( null );
		assertNotNull( boringFacts );
	}

	@Test
	void weirdFactsPublicationTypeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "domain.complex.domain" ).orElse( null );
		assertNotNull( domain );

		String typeGroup = typeRegistry.retrieveObjectType( WebCmsPublicationType.class ).orElse( null );
		WebCmsPublicationType weirdFacts = (WebCmsPublicationType) typeRepository.findOneByObjectTypeAndTypeKeyAndDomain(
				typeGroup, "domain.complex.domain:weird-facts", domain
		).orElse( null );
		assertNotNull( weirdFacts );
		assertEquals( domain, weirdFacts.getDomain() );
	}
}
