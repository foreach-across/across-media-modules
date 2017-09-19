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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsMultiDomainService
{
	private WebCmsDomain one = WebCmsDomain.builder().id( 123L ).build();

	@Mock
	private WebCmsMultiDomainConfiguration multiDomainConfiguration;

	@Mock
	private WebCmsDomainMetadataFactory metadataFactory;

	@InjectMocks
	private WebCmsMultiDomainServiceImpl multiDomainService;

	@Before
	public void before() {
		WebCmsDomainContextHolder.clearWebCmsDomainContext();
	}

	@After
	public void after() {
		WebCmsDomainContextHolder.clearWebCmsDomainContext();
	}

	@Test
	public void currentDomainIsNullIfNoneSet() {
		assertNull( multiDomainService.getCurrentDomain() );
	}

	@Test
	public void currentDomainIsTheDomainAttachedToTheContext() {
		WebCmsDomainContextHolder.setWebCmsDomainContext( new WebCmsDomainContext( one, null ) );
		assertSame( one, multiDomainService.getCurrentDomain() );
	}

	@Test
	public void getMetadataForDomainBuildsTheMetadata() {
		assertNull( multiDomainService.getMetadataForDomain( null, Long.class ) );
		verify( metadataFactory ).createMetadataForDomain( null );

		when( metadataFactory.createMetadataForDomain( one ) ).thenReturn( "metadata-for-one" );
		String metadata = multiDomainService.getMetadataForDomain( one, String.class );
		assertEquals( "metadata-for-one", metadata );
	}

	@Test
	public void currentDomainMetadataIsTakenFromTheContext() {
		assertNull( multiDomainService.getCurrentDomainMetadata( Object.class ) );

		WebCmsDomainContextHolder.setWebCmsDomainContext( new WebCmsDomainContext( one, null ) );
		assertNull( multiDomainService.getCurrentDomainMetadata( Object.class ) );

		WebCmsDomainContextHolder.setWebCmsDomainContext( new WebCmsDomainContext( one, "current metadata" ) );
		assertEquals( "current metadata", multiDomainService.getCurrentDomainMetadata( Object.class ) );
	}

	@SuppressWarnings("all")
	@Test
	public void nullValuesAreNeverDomainBound() {
		assertFalse( multiDomainService.isDomainBound( (WebCmsArticle) null ) );
		assertFalse( multiDomainService.isDomainBound( (Class<WebCmsObject>) null ) );
		verifyNoMoreInteractions( multiDomainConfiguration );
	}

	@Test
	public void currentDomainForTypeReturnsNullIfTypeNotBound() {
		when( multiDomainConfiguration.isDomainBound( WebCmsImage.class ) ).thenReturn( false );
		assertNull( multiDomainService.getCurrentDomainForType( WebCmsImage.class ) );

		WebCmsDomainContextHolder.setWebCmsDomainContext( new WebCmsDomainContext( one, null ) );
		assertNull( multiDomainService.getCurrentDomainForType( WebCmsImage.class ) );
	}

	@Test
	public void currentDomainForTypeReturnsCurrentDomainIfTypeIsBound() {
		when( multiDomainConfiguration.isDomainBound( WebCmsImage.class ) ).thenReturn( true );
		assertNull( multiDomainService.getCurrentDomainForType( WebCmsImage.class ) );

		WebCmsDomainContextHolder.setWebCmsDomainContext( new WebCmsDomainContext( one, null ) );
		assertSame( one, multiDomainService.getCurrentDomainForType( WebCmsImage.class ) );
	}

	@Test
	public void domainBoundTypes() {
		when( multiDomainConfiguration.isDomainBound( WebCmsArticle.class ) ).thenReturn( true );
		assertTrue( multiDomainService.isDomainBound( WebCmsArticle.class ) );
		assertFalse( multiDomainService.isDomainBound( WebCmsPage.class ) );
		verify( multiDomainConfiguration ).isDomainBound( WebCmsPage.class );
	}

	@Test
	public void domainBoundEntities() {
		when( multiDomainConfiguration.isDomainBound( WebCmsArticle.class ) ).thenReturn( true );
		assertTrue( multiDomainService.isDomainBound( WebCmsArticle.builder().build() ) );
		assertFalse( multiDomainService.isDomainBound( WebCmsPage.builder().build() ) );
		verify( multiDomainConfiguration ).isDomainBound( WebCmsPage.class );
	}

	@Test
	public void closeableDomainContext() {
		when( metadataFactory.createMetadataForDomain( one ) ).thenReturn( "metadata-one" );
		when( metadataFactory.createMetadataForDomain( null ) ).thenReturn( 123L );
		assertNull( multiDomainService.getCurrentDomain() );
		try (CloseableWebCmsDomainContext ignore = multiDomainService.attachDomainContext( one )) {
			assertSame( one, multiDomainService.getCurrentDomain() );
			assertEquals( "metadata-one", multiDomainService.getCurrentDomainMetadata( String.class ) );
			try (CloseableWebCmsDomainContext ignore2 = multiDomainService.attachDomainContext( WebCmsDomain.NONE )) {
				assertNull( multiDomainService.getCurrentDomain() );
				assertEquals( Long.valueOf( 123 ), multiDomainService.getCurrentDomainMetadata( Long.class ) );
			}
			assertSame( one, multiDomainService.getCurrentDomain() );
			assertEquals( "metadata-one", multiDomainService.getCurrentDomainMetadata( String.class ) );
		}
		assertNull( multiDomainService.getCurrentDomain() );
		assertNull( multiDomainService.getCurrentDomainMetadata( Object.class ) );
	}
}
