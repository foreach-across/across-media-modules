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

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageSection;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageSectionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsPageSectionValidator
{
	@Mock
	private WebCmsPageSectionRepository sectionRepository;

	@Mock
	private Errors errors;

	@InjectMocks
	private WebCmsPageSectionValidator validator;

	@Test
	public void supportWebCmsPageSectionOnly() {
		assertTrue( validator.supports( WebCmsPageSection.class ) );
		assertFalse( validator.supports( WebCmsPage.class ) );
	}

	@Test
	public void existingErrorOnName() {
		WebCmsPageSection section = new WebCmsPageSection();

		when( errors.hasFieldErrors( "name" ) ).thenReturn( true );
		validator.validate( section, errors );

		InOrder sequence = inOrder( errors, sectionRepository );
		sequence.verify( errors ).hasFieldErrors( "name" );
		verify( sectionRepository, never() ).findAllByPageOrderBySortIndexAscNameAsc( any( WebCmsPage.class ) );
		verifyNoMoreInteractions( errors );
	}

	@Test
	public void nameDoesNotExist() {
		WebCmsPageSection section = new WebCmsPageSection();
		section.setName( "section name" );
		WebCmsPage page = mock( WebCmsPage.class );
		section.setPage( page );

		when( sectionRepository.findAllByPageOrderBySortIndexAscNameAsc( page ) ).thenReturn(
				Collections.singleton( WebCmsPageSection.builder().name( "other" ).build() )
		);
		validator.validate( section, errors );

		InOrder sequence = inOrder( errors, sectionRepository );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( errors ).hasFieldErrors( "page" );
		sequence.verify( sectionRepository ).findAllByPageOrderBySortIndexAscNameAsc( page );
		verifyNoMoreInteractions( errors );
	}

	@Test
	public void sameSectionWithSameNameIsAllowed() {
		WebCmsPageSection section = new WebCmsPageSection();
		section.setName( "section name" );
		WebCmsPage page = mock( WebCmsPage.class );
		section.setPage( page );

		when( sectionRepository.findAllByPageOrderBySortIndexAscNameAsc( page ) ).thenReturn( Collections.singleton( section )
		);
		validator.validate( section, errors );

		InOrder sequence = inOrder( errors, sectionRepository );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( errors ).hasFieldErrors( "page" );
		sequence.verify( sectionRepository ).findAllByPageOrderBySortIndexAscNameAsc( page );
		verifyNoMoreInteractions( errors );
	}

	@Test
	public void otherSectionWithNameGivesError() {
		WebCmsPageSection section = new WebCmsPageSection();
		section.setName( "section name" );
		WebCmsPage page = mock( WebCmsPage.class );
		section.setPage( page );

		when( sectionRepository.findAllByPageOrderBySortIndexAscNameAsc( page ) ).thenReturn(
				Collections.singleton( WebCmsPageSection.builder().name( "SECTION NAME" ).build() )
		);
		validator.validate( section, errors );

		InOrder sequence = inOrder( errors, sectionRepository );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( errors ).hasFieldErrors( "page" );
		sequence.verify( sectionRepository ).findAllByPageOrderBySortIndexAscNameAsc( page );
		sequence.verify( errors ).rejectValue( "name", "alreadyExists" );
		verifyNoMoreInteractions( errors );
	}
}
