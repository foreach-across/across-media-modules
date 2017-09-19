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
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeRegistry;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import it.AbstractCmsApplicationWithTestDataIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ITTypeReferenceData extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsTypeSpecifierRepository typeRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsTypeRegistry typeRegistry;

	@Test
	public void teaserComponentTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsComponentType.class ).orElse( null );
		WebCmsComponentType teaser = (WebCmsComponentType) typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, "teaser" );
		assertNotNull( teaser );
	}

	@Test
	public void bigTeaserComponentTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsComponentType.class ).orElse( null );
		WebCmsComponentType bigTeaser = (WebCmsComponentType) typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, "domain.complex.domain:big-teaser" );
		assertNotNull( bigTeaser );
		assertEquals( domainRepository.findOneByDomainKey( "domain.complex.domain" ), bigTeaser.getDomain() );
	}

	@Test
	public void smallArticleArticleTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsArticleType.class ).orElse( null );
		WebCmsArticleType smallArticle = (WebCmsArticleType) typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, "small-article" );
		assertNotNull( smallArticle );
	}


	@Test
	public void mediumArticleArticleTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsArticleType.class ).orElse( null );
		WebCmsArticleType mediumArticle = (WebCmsArticleType) typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, "domain.complex.domain:medium-article" );
		assertNotNull( mediumArticle );
		assertEquals( domainRepository.findOneByDomainKey( "domain.complex.domain" ), mediumArticle.getDomain() );
	}

	@Test
	public void funFactsPublicationTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsPublicationType.class ).orElse( null );
		WebCmsPublicationType funFacts = (WebCmsPublicationType) typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, "fun-facts" );
		assertNotNull( funFacts );
	}

	@Test
	public void boringFactsPublicationTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsPublicationType.class ).orElse( null );
		WebCmsPublicationType boringFacts = (WebCmsPublicationType) typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, "boring-facts" );
		assertNotNull( boringFacts );
	}

	@Test
	public void weirdFactsPublicationTypeShouldHaveBeenImported() {
		String typeGroup = typeRegistry.retrieveObjectType( WebCmsPublicationType.class ).orElse( null );
		WebCmsPublicationType weirdFacts = (WebCmsPublicationType) typeRepository.findOneByObjectTypeAndTypeKey( typeGroup,
		                                                                                                         "domain.complex.domain:weird-facts" );
		assertNotNull( weirdFacts );
		assertEquals( domainRepository.findOneByDomainKey( "domain.complex.domain" ), weirdFacts.getDomain() );
	}
}
