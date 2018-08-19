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

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import it.AbstractCmsApplicationWithTestDataIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.Assert.*;

public class ITDomainReferenceData extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Test
	public void simpleDomainUnderscoreShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "simple_domain" ).orElse( null );
		assertNotNull( domain );
		assertEquals( "Simple domain with _", domain.getName() );
	}

	@Test
	public void domainWithObjectIdSimpleDomainShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByObjectId( "wcm:domain:simple-domain" ).orElse( null );
		assertNotNull( domain );
		assertEquals( "Simple domain with ObjectId simple-domain", domain.getName() );
		assertEquals( "domain.simple.domain", domain.getDomainKey() );
		assertFalse( domain.isActive() );
	}

	@Test
	public void simpleDomainDotShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "simple.domain" ).orElse( null );
		assertNotNull( domain );
		assertEquals( "Name of simple.domain should have been replaced", domain.getName() );
		assertEquals( "A simple domain needs a simple description", domain.getDescription() );
	}

	@Test
	public void domainWithObjectIdDomainWithIdShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByObjectId( "wcm:domain:domain-with-id" ).orElse( null );
		assertNotNull( domain );
		assertEquals( "domain.domain_with_id", domain.getDomainKey() );
		assertEquals( "Simple domain with ObjectId", domain.getName() );
	}

	@Test
	public void deleteMeDomainShouldHaveBeenDeleted() {
		assertEquals( Optional.empty(), domainRepository.findOneByDomainKey( "deleteMeDomain" ) );
	}

	@Test
	public void complexDomainShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByObjectId( "wcm:domain:complex-domain" ).orElse( null );
		assertNotNull( domain );
		assertEquals( "A complex domain", domain.getName() );
		assertTrue( domain.isActive() );
		assertEquals( "127.0.0.1", domain.getAttributes().get( "dns" ) );
		assertEquals( "I love cookies", domain.getAttributes().get( "cookies" ) );
		assertEquals( 2, domain.getAttributes().size() );
	}

	@Test
	public void cookiesDomainShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "domain.cookies" ).orElse( null );
		assertNotNull( domain );
		assertEquals( "Cookies", domain.getName() );
		assertEquals( "Where cookies live", domain.getDescription() );
		assertEquals( "999", domain.getAttributes().get( "cookies" ) );
		assertEquals( 1, domain.getAttributes().size() );
	}
}
