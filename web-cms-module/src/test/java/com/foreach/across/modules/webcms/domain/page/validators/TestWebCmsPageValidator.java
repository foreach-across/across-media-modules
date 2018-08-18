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

package com.foreach.across.modules.webcms.domain.page.validators;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsPageValidator
{
	@Mock
	private WebCmsPageService pageService;

	@Mock
	private WebCmsPageRepository pageRepository;

	@Mock
	private Errors errors;

	@InjectMocks
	private WebCmsPageValidator validator;

	@Test
	public void supportsWebCmsPageOnly() {
		assertTrue( validator.supports( WebCmsPage.class ) );
	}

	@Test
	public void existingErrorOnCanonicalPath() {
		WebCmsPage page = new WebCmsPage();

		when( errors.hasFieldErrors( "canonicalPath" ) ).thenReturn( true );
		validator.validate( page, errors );

		InOrder sequence = inOrder( pageService, pageRepository, errors );
		sequence.verify( pageService ).prepareForSaving( page );
		sequence.verify( errors ).hasFieldErrors( "canonicalPath" );
		sequence.verify( pageRepository, never() ).findOneByCanonicalPathAndDomain( anyString(), any() );
		verifyNoMoreInteractions( errors );
	}

	@Test
	public void noExistingPage() {
		WebCmsPage page = new WebCmsPage();
		page.setCanonicalPath( "canonical path" );

		when( pageRepository.findOneByCanonicalPathAndDomain( "canonical path", null ) ).thenReturn( null );

		validator.validate( page, errors );

		InOrder sequence = inOrder( pageService, pageRepository, errors );
		sequence.verify( pageService ).prepareForSaving( page );
		sequence.verify( errors ).hasFieldErrors( "canonicalPath" );
		sequence.verify( pageRepository ).findOneByCanonicalPathAndDomain( "canonical path", null );
		verifyNoMoreInteractions( errors );
	}

	@Test
	public void samePageWithSamePath() {
		WebCmsPage page = new WebCmsPage();
		page.setCanonicalPath( "canonical path" );

		when( pageRepository.findOneByCanonicalPathAndDomain( "canonical path", null ) ).thenReturn( page );

		validator.validate( page, errors );

		InOrder sequence = inOrder( pageService, pageRepository, errors );
		sequence.verify( pageService ).prepareForSaving( page );
		sequence.verify( errors ).hasFieldErrors( "canonicalPath" );
		sequence.verify( pageRepository ).findOneByCanonicalPathAndDomain( "canonical path", null );
		verifyNoMoreInteractions( errors );
	}

	@Test
	public void canonicalPathMustBeUnique() {
		WebCmsPage page = new WebCmsPage();
		page.setCanonicalPath( "canonical path" );

		when( pageRepository.findOneByCanonicalPathAndDomain( "canonical path", null ) ).thenReturn( mock( WebCmsPage.class ) );

		validator.validate( page, errors );

		InOrder sequence = inOrder( pageService, pageRepository, errors );
		sequence.verify( pageService ).prepareForSaving( page );
		sequence.verify( errors ).hasFieldErrors( "canonicalPath" );
		sequence.verify( pageRepository ).findOneByCanonicalPathAndDomain( "canonical path", null );
		sequence.verify( errors ).rejectValue( "canonicalPath", "alreadyExists" );
		verifyNoMoreInteractions( errors );
	}

	@Test
	public void canonicalPathMustBeUniqueWithinDomain() {
		WebCmsDomain domain = WebCmsDomain.builder()
		                                  .domainKey( "my-domain" )
		                                  .build();
		WebCmsPage page = new WebCmsPage();
		page.setCanonicalPath( "canonical path" );
		page.setDomain( domain );

		when( pageRepository.findOneByCanonicalPathAndDomain( "canonical path", domain ) ).thenReturn( page );

		WebCmsPage newPage = WebCmsPage.builder().canonicalPath( "canonical path" )
		                               .domain( domain )
		                               .build();

		validator.validate( newPage, errors );
		InOrder sequence = inOrder( pageService, pageRepository, errors );
		sequence.verify( pageService ).prepareForSaving( newPage );
		sequence.verify( errors ).hasFieldErrors( "canonicalPath" );
		sequence.verify( pageRepository ).findOneByCanonicalPathAndDomain( "canonical path", domain );
		sequence.verify( errors ).rejectValue( "canonicalPath", "alreadyExists" );
		verifyNoMoreInteractions( errors );

	}
}
