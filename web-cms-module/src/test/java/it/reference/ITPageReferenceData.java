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

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import it.AbstractCmsApplicationWithTestDataIT;
import lombok.val;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ITPageReferenceData extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsEndpointService endpointService;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Test
	public void alwaysCreatedPageShouldBeCreatedWithBothObjectIdAndCanonicalPathGenerated() {
		val page = pageRepository.findOneByCanonicalPath( "/always-created-page" );
		assertNotNull( page );
		assertNull( page.getParent() );
		assertEquals( "Always Created Page", page.getTitle() );
		assertTrue( page.isPathSegmentGenerated() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertEquals( "always-created-page", page.getPathSegment() );
		assertEquals( "/always-created-page", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/always-created-page" );
		assertFalse( page.isPublished() );
		assertNull( page.getPublicationDate() );
	}

	@Test
	public void simplePageShouldHaveItsPathGeneratedAndShouldNotBePublished() {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-simple" );
		assertNotNull( page );
		assertNull( page.getParent() );
		assertEquals( "Simple Page", page.getTitle() );
		assertTrue( page.isPathSegmentGenerated() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertEquals( "simple-page", page.getPathSegment() );
		assertEquals( "/simple-page", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/simple-page" );
		assertFalse( page.isPublished() );
		assertNull( page.getPublicationDate() );
	}

	@Test
	public void childPageShouldHaveItsPathGeneratedAndShouldBePublished() {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-simple-child" );
		assertNotNull( page );
		assertEquals( "Simple Child Page", page.getTitle() );
		assertNotNull( page.getParent() );
		assertEquals( "wcm:asset:page:reference-simple", page.getParent().getObjectId() );
		assertTrue( page.isPathSegmentGenerated() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertEquals( "simple-child-page", page.getPathSegment() );
		assertEquals( "/simple-page/simple-child-page", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/simple-page/simple-child-page" );
		assertTrue( page.isPublished() );
		assertNotNull( page.getPublicationDate() );
	}

	@Test
	public void fixedPathPageShouldHaveNonGeneratedPathSegmentAndPublicationDateSet() throws Exception {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-fixed-path-segment" );
		assertNotNull( page );
		assertEquals( "Fixed Path Segment Page", page.getTitle() );
		assertNotNull( page.getParent() );
		assertEquals( "wcm:asset:page:reference-simple", page.getParent().getObjectId() );
		assertFalse( page.isPathSegmentGenerated() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertEquals( "fixed", page.getPathSegment() );
		assertEquals( "/simple-page/fixed", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/simple-page/fixed" );
		assertFalse( page.isPublished() );
		assertEquals( DateUtils.parseDate( "2017-03-14", "yyyy-MM-dd" ), page.getPublicationDate() );
	}

	@Test
	public void fixedCanonicalPathPageShouldHaveNonGeneratedCanonicalPathAndShouldBePublishedWithPublicationDate() throws Exception {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-fixed-canonical-path" );
		assertNotNull( page );
		assertEquals( "Fixed Canonical Path Page", page.getTitle() );
		assertNotNull( page.getParent() );
		assertEquals( "wcm:asset:page:reference-simple", page.getParent().getObjectId() );
		assertTrue( page.isPathSegmentGenerated() );
		assertFalse( page.isCanonicalPathGenerated() );
		assertEquals( "fixed-canonical-path-page", page.getPathSegment() );
		assertEquals( "/fixed/canonical-path", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/fixed/canonical-path" );
		assertTrue( page.isPublished() );
		assertEquals( DateUtils.parseDate( "2016-05-11 16:30:00", "yyyy-MM-dd HH:mm:ss" ), page.getPublicationDate() );
	}

	@Test
	public void extensionPageShouldBeCreatedWithKeyAsCanonicalPath() {
		val page = pageRepository.findOneByCanonicalPath( "/extension/one" );
		assertNotNull( page );
		assertNull( page.getParent() );
		assertEquals( "Extension Page One", page.getTitle() );
		assertTrue( page.isPathSegmentGenerated() );
		assertFalse( page.isCanonicalPathGenerated() );
		assertEquals( "extension-page-one", page.getPathSegment() );
		assertEquals( "/extension/one", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/extension/one" );
		assertFalse( page.isPublished() );
		assertNull( page.getPublicationDate() );
	}

	@Test
	public void updateForNonExistingPageShouldBeIgnored() {
		assertNull( pageRepository.findOneByCanonicalPath( "/extension/two" ) );
	}

	@Test
	public void existingPageShouldBeDeleted() {
		assertNull( pageRepository.findOneByCanonicalPath( "/extension/deleteme" ) );
	}

	@Test
	public void existingPageShouldBeUpdatedUsingTheCanonicalPathAsKeyLookup() throws Exception {
		assertNull( pageRepository.findOneByCanonicalPath( "/extension/updateme1" ) );

		val page = pageRepository.findOneByCanonicalPath( "/extension/updated1" );
		assertNotNull( page );
		assertEquals( "Extension Updated 1", page.getTitle() );
		assertNull( page.getParent() );
		assertTrue( page.isPathSegmentGenerated() );
		assertFalse( page.isCanonicalPathGenerated() );
		assertEquals( "extension-updated-1", page.getPathSegment() );
		assertEquals( "/extension/updated1", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/extension/updated1" );
		assertTrue( page.isPublished() );
		assertEquals( DateUtils.parseDate( "2015-11-15", "yyyy-MM-dd" ), page.getPublicationDate() );
	}

	@Test
	public void existingPageShouldBeUpdatedUsingTheObjectIdAndKeyShouldBeIgnored() {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:extension-updateme2" );
		assertNotNull( page );
		assertEquals( "Extension Updated 2", page.getTitle() );
		assertNull( page.getParent() );
		assertTrue( page.isPathSegmentGenerated() );
		assertFalse( page.isCanonicalPathGenerated() );
		assertEquals( "extension-updated-2", page.getPathSegment() );
		assertEquals( "/extension/updateme2", page.getCanonicalPath() );
		assertPrimaryUrl( page, "/extension/updateme2" );
		assertFalse( page.isPublished() );
		assertNull( page.getPublicationDate() );
	}

	private void assertPrimaryUrl( WebCmsAsset asset, String path ) {
		WebCmsUrl url = endpointService.getPrimaryUrlForAsset( asset ).orElse( null );
		assertNotNull( url );
		assertEquals( path, url.getPath() );
		assertTrue( url.isPrimary() );
		assertEquals( HttpStatus.OK, url.getHttpStatus() );
	}

	@Test
	public void frequentlyUsedDomainsPageShouldHaveBeenImported() {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-freq-domains" );
		assertNotNull( page );
		assertEquals( domainRepository.findOneByObjectId( "wcm:domain:simple-domain" ), page.getDomain() );
	}

	@Test
	public void generalDomainsPageShouldHaveBeenImported() {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-gen-domains" );
		assertNotNull( page );
		assertEquals( domainRepository.findOneByDomainKey( "domain.complex.domain" ), page.getDomain() );
	}

	@Test
	public void commonDomainsPageShouldHaveBeenImportedAndExtended() {
		val page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-common-domains" );
		assertNotNull( page );
		assertNull( page.getDomain() );
	}
}
