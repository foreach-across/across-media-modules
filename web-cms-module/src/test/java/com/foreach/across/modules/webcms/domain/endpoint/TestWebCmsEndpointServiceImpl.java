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

package com.foreach.across.modules.webcms.domain.endpoint;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsEndpointServiceImpl
{
	@Mock
	private WebCmsUrlRepository urlRepository;

	@Mock
	private WebCmsAssetEndpointRepository assetEndpointRepository;

	@InjectMocks
	private WebCmsEndpointServiceImpl endpointService;

	private WebCmsPage page;
	private WebCmsUrl existingUrl;
	private WebCmsAssetEndpoint<WebCmsPage> endpoint;

	@Before
	public void setUp() throws Exception {
		page = WebCmsPage.builder().build();
		endpoint = WebCmsAssetEndpoint.<WebCmsPage>builder().asset( page ).build();
		existingUrl = WebCmsUrl.builder().id( 123L ).path( "/test" ).primary( true ).httpStatus( HttpStatus.OK ).build();
		endpoint.setUrls( Collections.singletonList( existingUrl ) );

		when( assetEndpointRepository.findOneByAsset( page ) ).thenReturn( endpoint );
	}

	@Test
	public void noEndpointReturnsNoPrimaryUrl() {
		when( assetEndpointRepository.findOneByAsset( page ) ).thenReturn( null );
		assertEquals( Optional.empty(), endpointService.updateOrCreatePrimaryUrlForAsset( "/my/page", page ) );
	}

	@Test
	public void primaryUrlAlwaysGetsCreated() {
		endpoint.setUrls( Collections.emptyList() );

		AtomicReference<WebCmsUrl> created = new AtomicReference<>();

		doAnswer( invocationOnMock -> {
			created.set( invocationOnMock.getArgumentAt( 0, WebCmsUrl.class ) );
			return null;
		} ).when( urlRepository ).save( any( WebCmsUrl.class ) );

		Optional<WebCmsUrl> url = endpointService.updateOrCreatePrimaryUrlForAsset( "/my/page", page );
		assertTrue( url.isPresent() );

		WebCmsUrl primaryUrl = created.get();
		assertNotNull( primaryUrl );
		assertEquals( "/my/page", primaryUrl.getPath() );
		assertEquals( HttpStatus.OK, primaryUrl.getHttpStatus() );
		assertTrue( primaryUrl.isPrimary() );

		assertEquals( primaryUrl, url.get() );
	}

	@Test
	public void primaryUrlGetsUpdatedIfPublicationDateIsNotSet() {
		AtomicReference<WebCmsUrl> created = new AtomicReference<>();

		doAnswer( invocationOnMock -> {
			created.set( invocationOnMock.getArgumentAt( 0, WebCmsUrl.class ) );
			return null;
		} ).when( urlRepository ).save( any( WebCmsUrl.class ) );

		Optional<WebCmsUrl> url = endpointService.updateOrCreatePrimaryUrlForAsset( "/my/page", page );
		assertTrue( url.isPresent() );

		WebCmsUrl primaryUrl = created.get();
		assertNotNull( primaryUrl );
		assertEquals( existingUrl, primaryUrl );
		assertEquals( "/my/page", primaryUrl.getPath() );
		assertEquals( HttpStatus.OK, primaryUrl.getHttpStatus() );
		assertTrue( primaryUrl.isPrimary() );

		assertEquals( primaryUrl, url.get() );
	}

	@Test
	public void primaryUrlGetsUpdatedIfPublicationDateIsFuture() {
		AtomicReference<WebCmsUrl> created = new AtomicReference<>();

		page.setPublished( true );
		page.setPublicationDate( DateUtils.addYears( new Date(), 1 ) );

		doAnswer( invocationOnMock -> {
			created.set( invocationOnMock.getArgumentAt( 0, WebCmsUrl.class ) );
			return null;
		} ).when( urlRepository ).save( any( WebCmsUrl.class ) );

		Optional<WebCmsUrl> url = endpointService.updateOrCreatePrimaryUrlForAsset( "/my/page", page );
		assertTrue( url.isPresent() );

		WebCmsUrl primaryUrl = created.get();
		assertNotNull( primaryUrl );
		assertEquals( existingUrl, primaryUrl );
		assertEquals( "/my/page", primaryUrl.getPath() );
		assertEquals( HttpStatus.OK, primaryUrl.getHttpStatus() );
		assertTrue( primaryUrl.isPrimary() );

		assertEquals( primaryUrl, url.get() );
	}

	@Test
	public void redirectUrlGetsCreatedIfPublishedPageIsUpdated() {
		AtomicReference<WebCmsUrl> created = new AtomicReference<>();
		AtomicReference<WebCmsUrl> updated = new AtomicReference<>();

		page.setPublished( true );
		page.setPublicationDate( new Date() );

		doAnswer( invocationOnMock -> {
			WebCmsUrl url = invocationOnMock.getArgumentAt( 0, WebCmsUrl.class );
			if ( url.isNew() ) {
				created.set( url );
			}
			else {
				updated.set( url );
			}
			return null;
		} ).when( urlRepository ).save( any( WebCmsUrl.class ) );

		Optional<WebCmsUrl> url = endpointService.updateOrCreatePrimaryUrlForAsset( "/my/page", page );
		assertTrue( url.isPresent() );

		WebCmsUrl redirect = updated.get();
		assertNotNull( redirect );
		assertEquals( "/test", redirect.getPath() );
		assertEquals( HttpStatus.MOVED_PERMANENTLY, redirect.getHttpStatus() );
		assertFalse( redirect.isPrimary() );

		WebCmsUrl primaryUrl = created.get();
		assertNotNull( primaryUrl );
		assertEquals( "/my/page", primaryUrl.getPath() );
		assertEquals( HttpStatus.OK, primaryUrl.getHttpStatus() );
		assertTrue( primaryUrl.isPrimary() );

		assertEquals( primaryUrl, url.get() );
	}

	@Test
	public void redirectUrlGetsCreatedIfNotPublishedButPublicationDateIsPast() {
		AtomicReference<WebCmsUrl> created = new AtomicReference<>();
		AtomicReference<WebCmsUrl> updated = new AtomicReference<>();

		page.setPublished( false );
		page.setPublicationDate( DateUtils.addYears( new Date(), -1 ) );

		doAnswer( invocationOnMock -> {
			WebCmsUrl url = invocationOnMock.getArgumentAt( 0, WebCmsUrl.class );
			if ( url.isNew() ) {
				created.set( url );
			}
			else {
				updated.set( url );
			}
			return null;
		} ).when( urlRepository ).save( any( WebCmsUrl.class ) );

		Optional<WebCmsUrl> url = endpointService.updateOrCreatePrimaryUrlForAsset( "/my/page", page );
		assertTrue( url.isPresent() );

		WebCmsUrl redirect = updated.get();
		assertNotNull( redirect );
		assertEquals( "/test", redirect.getPath() );
		assertEquals( HttpStatus.MOVED_PERMANENTLY, redirect.getHttpStatus() );
		assertFalse( redirect.isPrimary() );

		WebCmsUrl primaryUrl = created.get();
		assertNotNull( primaryUrl );
		assertEquals( "/my/page", primaryUrl.getPath() );
		assertEquals( HttpStatus.OK, primaryUrl.getHttpStatus() );
		assertTrue( primaryUrl.isPrimary() );

		assertEquals( primaryUrl, url.get() );
	}
}
