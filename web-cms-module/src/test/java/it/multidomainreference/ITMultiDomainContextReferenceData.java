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

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

class ITMultiDomainContextReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsTypeSpecifierRepository typeSpecifierRepository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsComponentRepository componentRepository;

	@Test
	void coolFactsDePublicationTypeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsTypeSpecifier publicationType
				= typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "publication", "de-foreach:cool-facts", domain );
		assertNotNull( publicationType );
		assertEquals( "Cool Facts (DE)", publicationType.getName() );
		assertEquals( domain, publicationType.getDomain() );
	}

	@Test
	void poolRequestDePageShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/pool-request", domain );
		assertNotNull( page );
		assertEquals( "Pool Request (DE)", page.getTitle() );
	}

	@Test
	void coolFactsNlPublicationTypeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsTypeSpecifier publicationType
				= typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "publication", "nl-foreach:cool-facts", domain );
		assertNotNull( publicationType );
		assertEquals( "Cool Facts (NL)", publicationType.getName() );
		assertEquals( domain, publicationType.getDomain() );
	}

	@Test
	void poolRequestNlPageShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/pool-request", domain );
		assertNotNull( page );
		assertEquals( "Pool Request (NL)", page.getTitle() );
	}

	@Test
	void myComponentNlTypeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsTypeSpecifier componentType
				= typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "component", "nl-foreach:my-component", domain );
		assertNotNull( componentType );
		assertEquals( domain, componentType.getDomain() );
	}

	@Test
	void nlComponentShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "nl-component", domain );
		assertNotNull( component );
		assertEquals( "My component asset (NL)", component.getTitle() );
		List<WebCmsComponent> children = componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( component.getObjectId(),
		                                                                                                         component.getDomain() );
		assertEquals( 1, children.size() );
		assertEquals( "My Component Title (NL)", children.get( 0 ).getBody() );
		assertEquals( domain, children.get( 0 ).getDomain() );
	}

	@Test
	void coolFactsAnotherForeachShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "another-foreach" );
		WebCmsTypeSpecifier publicationType
				= typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "publication", "another-foreach:cool-facts", domain );
		assertNotNull( publicationType );
		assertEquals( "Cool Facts (another-foreach)", publicationType.getName() );
		assertEquals( domain, publicationType.getDomain() );
	}

	@Test
	void myComponentTypeShouldHaveBeenImported() {
		WebCmsTypeSpecifier componentType = typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( "component", "my-component", WebCmsDomain.NONE );
		assertNotNull( componentType );
		assertNull( componentType.getDomain() );
	}

	@Test
	void poolRequestAnotherForeachShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "another-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/pool-request", domain );
		assertNotNull( page );
		assertEquals( "Pool Request (another-foreach)", page.getTitle() );
	}

	@Test
	void myComponentShouldHaveBeenImported() {
		WebCmsComponent component = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( null, "my-component", WebCmsDomain.NONE );
		assertNotNull( component );
		assertEquals( "My component asset", component.getTitle() );
		List<WebCmsComponent> children
				= componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( component.getObjectId(), component.getDomain() );
		assertEquals( 1, children.size() );
		assertEquals( "My Component Title", children.get( 0 ).getBody() );
		assertNull( children.get( 0 ).getDomain() );
	}
}
