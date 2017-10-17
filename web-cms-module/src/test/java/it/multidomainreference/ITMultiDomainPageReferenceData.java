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

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

public class ITMultiDomainPageReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsAssetEndpointRepository assetEndpointRepository;

	@Test
	public void faqPageForeachNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/faq", domain );
		assertNotNull( page );
	}

	@Test
	public void faqPageForeachBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/faq", domain );
		assertNotNull( page );
	}

	@Test
	public void faqPageForeachDeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/faq", domain );
		assertNotNull( page );
	}

	@Test
	public void cafePageNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/cafe", domain );
		assertNotNull( page );
		assertEquals( "Foreach Cafe (NL)", page.getTitle() );
	}

	@Test
	public void cafePageBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/cafe", domain );
		assertNotNull( page );
		assertEquals( "Foreach Cafe (BE)", page.getTitle() );
	}

	@Test
	public void cafePageDeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/cafe", domain );
		assertNotNull( page );
		assertEquals( "Foreach Cafe (DE)", page.getTitle() );
	}

	@Test
	public void myPageBeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-page", domain );
		assertNull( page );
	}

	@Test
	public void myPageNlShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-page", domain );
		assertNotNull( page );
		assertEquals( "My Page (NL)", page.getTitle() );
	}

	@Test
	public void myOtherPageBeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-other-page", domain );
		assertNull( page );
	}

	@Test
	public void myOtherPageDeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-other-page", domain );
		assertNull( page );
	}

	@Test
	public void myOtherPageNlShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-other-page", domain );
		assertNotNull( page );
		assertEquals( "My Other Page (NL)", page.getTitle() );
	}

	@Test
	@Transactional
	public void pageWithUrlShouldHaveBeenImported() {
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/page-with-url", WebCmsDomain.NONE );
		assertNotNull( page );
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( page, WebCmsDomain.NONE );
		assertEquals( 3, endpoint.getUrls().size() );
		WebCmsUrl url = endpoint.getUrlWithPath( "/page-with-url" ).orElse( null );
		assertNotNull( url );
		assertTrue( url.isPrimary() );
		assertEquals( HttpStatus.valueOf( 200 ), url.getHttpStatus() );

		WebCmsUrl testUrl = endpoint.getUrlWithPath( "/test-url" ).orElse( null );
		assertNotNull( testUrl );
		assertFalse( testUrl.isPrimary() );
		assertEquals( HttpStatus.valueOf( 301 ), testUrl.getHttpStatus() );

		WebCmsUrl myOtherUrl = endpoint.getUrlWithPath( "/my-other-url" ).orElse( null );
		assertNotNull( myOtherUrl );
		assertFalse( myOtherUrl.isPrimary() );
		assertEquals( HttpStatus.valueOf( 200 ), myOtherUrl.getHttpStatus() );
	}

	@Test
	@Transactional
	public void otherPageWithUrlShouldHaveBeenImportedAndExtended() {
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/other-page-with-url", WebCmsDomain.NONE );
		assertNotNull( page );
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( page, WebCmsDomain.NONE );
		assertEquals( 2, endpoint.getUrls().size() );
		WebCmsUrl url = endpoint.getUrlWithPath( "/other-page-with-url" ).orElse( null );
		assertNotNull( url );
		assertTrue( url.isPrimary() );
		assertEquals( HttpStatus.valueOf( 200 ), url.getHttpStatus() );

		WebCmsUrl testUrl = endpoint.getUrlWithPath( "/other-test-url" ).orElse( null );
		assertNotNull( testUrl );
		assertFalse( testUrl.isPrimary() );
		assertEquals( HttpStatus.valueOf( 301 ), testUrl.getHttpStatus() );

		WebCmsUrl myOtherUrl = endpoint.getUrlWithPath( "/to-be-deleted" ).orElse( null );
		assertNull( myOtherUrl );
	}
}
