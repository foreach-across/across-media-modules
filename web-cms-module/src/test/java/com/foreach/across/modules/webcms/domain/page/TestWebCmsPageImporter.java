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

package com.foreach.across.modules.webcms.domain.page;

import com.foreach.across.modules.webcms.data.*;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import com.foreach.across.modules.webcms.domain.page.validators.WebCmsPageValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsPageImporter
{
	@Mock
	private WebCmsAssetRepository assetRepository;

	@Mock
	private WebCmsDataConversionService conversionService;

	@Mock
	private WebCmsPageService pageService;

	@Mock
	private WebCmsMultiDomainService multiDomainService;

	private WebCmsPropertyDataImportService propertyDataImportService = new WebCmsPropertyDataImportService();

	@Mock
	private WebCmsPageValidator pageValidator;

	@InjectMocks
	private WebCmsPageImporter pageImporter;

	@Before
	public void setUp() throws Exception {
		pageImporter.setPropertyDataImportService( propertyDataImportService );
	}

	@Test(expected = WebCmsDataImportException.class)
	public void validateObjectIdFailsIfNotMatchingObjectId() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .data( Collections.singletonMap( "objectId", "wcm:assets:page:invalid-page-id" ) )
		                                      .build();
		pageImporter.importData( data );
	}

	@Test
	public void validateObjectIdWithMatchingObjectId() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .data( Collections.singletonMap( "objectId", "wcm:asset:page:valid-page-id" ) )
		                                      .build();
		when( multiDomainService.isDomainBound( any( WebCmsObject.class ) ) ).thenReturn( false );
		pageImporter.importData( data );
	}

	@Test
	public void createNewDto() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .key( "/my-page-key" )
		                                      .data( Collections.singletonMap( "title", "My page" ) )
		                                      .build();
		WebCmsPage dto = pageImporter.createDto( data, null, WebCmsDataAction.CREATE, null );
		assertEquals( "/my-page-key", dto.getCanonicalPath() );
		assertFalse( dto.isCanonicalPathGenerated() );
		assertNull( dto.getPathSegment() );
		assertTrue( dto.isPathSegmentGenerated() );
		assertNull( dto.getTitle() );
	}

	@Test
	public void createNewDtoWithEntryKeyNullAndCanonicalPath() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .data( Collections.singletonMap( "canonicalPath", "/my-page-key" ) )
		                                      .build();
		WebCmsPage dto = pageImporter.createDto( data, null, WebCmsDataAction.CREATE, null );
		assertNull( dto.getCanonicalPath() );
		assertFalse( dto.isCanonicalPathGenerated() );
		assertNull( dto.getPathSegment() );
		assertTrue( dto.isPathSegmentGenerated() );
	}

	@Test
	public void createDtoUpdate() {
		WebCmsPage existing = WebCmsPage.builder()
		                                .title( "My title" )
		                                .build();
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .data( Collections.singletonMap( "canonicalPath", "/my-page-key" ) )
		                                      .build();
		WebCmsPage dto = pageImporter.createDto( data, existing, WebCmsDataAction.UPDATE, null );
		assertEquals( dto.getTitle(), existing.getTitle() );
		assertNull( dto.getCanonicalPath() );
		assertFalse( dto.isCanonicalPathGenerated() );
		assertNull( dto.getPathSegment() );
		assertTrue( dto.isPathSegmentGenerated() );
	}

	@Test
	public void createDtoReplace() {
		WebCmsPage existing = WebCmsPage.builder()
		                                .objectId( "wcm:asset:page:my-page" )
		                                .id( 1L )
		                                .createdBy( "admin" )
		                                .createdDate( new Date() )
		                                .build();
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .data( Collections.singletonMap( "canonicalPath", "/my-page-key" ) )
		                                      .build();

		WebCmsPage dto = pageImporter.createDto( data, existing, WebCmsDataAction.REPLACE, null );
		assertEquals( "wcm:asset:page:my-page", existing.getObjectId() );
		assertEquals( 1L, dto.getId().longValue() );
		assertEquals( dto.getCreatedBy(), existing.getCreatedBy() );
		assertEquals( dto.getCreatedDate(), existing.getCreatedDate() );
		assertNull( dto.getCanonicalPath() );
		assertFalse( dto.isCanonicalPathGenerated() );
		assertNull( dto.getPathSegment() );
		assertTrue( dto.isPathSegmentGenerated() );
	}

	@Test
	public void getExistingEntityWithEntryKeyNotNull() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .key( "/my-page-key" )
		                                      .data( Collections.singletonMap( "objectId", "wcm:asset:page:valid-page-id" ) )
		                                      .build();
		WebCmsPage page = WebCmsPage.builder()
		                            .canonicalPath( "/my-page-key" )
		                            .domain( null )
		                            .build();
		when( pageService.findByCanonicalPathAndDomain( any(), any() ) ).thenReturn( java.util.Optional.ofNullable( page ) );
		WebCmsPage retrievedPage = pageImporter.getExistingEntity( "/my-page-key", data, null );
		assertNotNull( retrievedPage );

	}

	@Test
	public void getExistingEntityWithEntryKeyNull() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .data( Collections.singletonMap( "canonicalPath", "/my-page-key" ) )
		                                      .build();
		WebCmsPage page = WebCmsPage.builder()
		                            .canonicalPath( "/my-page-key" )
		                            .domain( null )
		                            .build();
		when( pageService.findByCanonicalPathAndDomain( any(), any() ) ).thenReturn( java.util.Optional.ofNullable( page ) );
		WebCmsPage retrievedPage = pageImporter.getExistingEntity( null, data, null );
		assertNotNull( retrievedPage );
	}

	@Test
	public void getExistingEntityWithEntryKeyNullAndCanonicalPathNull() {
		WebCmsDataEntry data = WebCmsDataEntry.builder()
		                                      .data( Collections.singletonMap( "title", "My title" ) )
		                                      .build();
		WebCmsPage retrievedPage = pageImporter.getExistingEntity( null, data, null );
		assertNull( retrievedPage );
	}
}
