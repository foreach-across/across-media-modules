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

package com.foreach.across.modules.webcms.domain.component;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsComponentValidator
{
	@Mock
	private Errors errors;

	@Mock
	private WebCmsComponentRepository componentRepository;

	@InjectMocks
	private WebCmsComponentValidator validator;

	@Test
	public void nameIsAssignedBasedOnTitleIfNotSet() {
		WebCmsComponent component = WebCmsComponent.builder().title( "My title" ).build();
		validator.preValidation( component, errors );

		assertEquals( "My title", component.getTitle() );
		assertEquals( "my-title", component.getName() );
	}

	@Test
	public void assignedNameIsKept() {
		WebCmsComponent component = WebCmsComponent.builder().title( "My title" ).name( "some name" ).build();
		validator.preValidation( component, errors );

		assertEquals( "My title", component.getTitle() );
		assertEquals( "some name", component.getName() );
	}

	@Test
	public void nameLookupDoesNotHappenIfAlreadyErrors() {
		WebCmsComponent component = WebCmsComponent.builder().name( "some name" ).build();
		when( errors.hasFieldErrors( "name" ) ).thenReturn( true );
		validator.postValidation( component, errors );
		verifyNoMoreInteractions( componentRepository );
	}

	@Test
	public void nameMustBeUniqueWithinTheOwner() {
		WebCmsComponent existing = WebCmsComponent.builder().id( 1L ).name( "other" ).build();
		when( componentRepository.findOneByOwnerObjectIdAndName( any(), eq( "some name" ) ) ).thenReturn( existing );

		WebCmsComponent component = WebCmsComponent.builder().name( "some name" ).build();
		validator.postValidation( component, errors );

		verify( errors ).rejectValue( "name", "alreadyExists" );
		verify( componentRepository ).findOneByOwnerObjectIdAndName( null, "some name" );

		reset( errors );
		validator.postValidation( WebCmsComponent.builder().ownerObjectId( "123" ).name( "some name" ).build(), errors );
		verify( errors ).rejectValue( "name", "alreadyExists" );
		verify( componentRepository ).findOneByOwnerObjectIdAndName( "123", "some name" );
	}

	@Test
	public void nameLookupShouldHappen() {
		WebCmsComponent component = WebCmsComponent.builder().name( "some name" ).build();
		validator.postValidation( component, errors );
		verify( componentRepository ).findOneByOwnerObjectIdAndName( null, "some name" );
		verify( errors, never() ).rejectValue( "name", "alreadyExists" );
	}

	@Test
	public void sameInstanceWithSameNameIsNotAnError() {
		WebCmsComponent existing = WebCmsComponent.builder().id( 1L ).name( "other" ).build();
		when( componentRepository.findOneByOwnerObjectIdAndName( any(), eq( "some name" ) ) ).thenReturn( existing );

		WebCmsComponent component = WebCmsComponent.builder().id( 1L ).name( "some name" ).build();
		validator.postValidation( component, errors );

		verify( errors, never() ).rejectValue( "name", "alreadyExists" );
	}
}