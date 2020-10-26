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

package com.foreach.across.modules.webcms.domain.publication;

import com.foreach.across.modules.webcms.domain.publication.web.WebCmsPublicationValidator;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestWebCmsPublicationValidator
{
	@Mock
	private Errors errors;

	@Mock
	private WebCmsPublicationRepository publicationRepository;

	@InjectMocks
	private WebCmsPublicationValidator validator;

	@Test
	public void supportsWebCmsPublicationOnly() {
		assertTrue( validator.supports( WebCmsPublication.class ) );
	}

	@Test
	public void existingErrorOnName() {
		WebCmsPublication publication = new WebCmsPublication();

		when( errors.hasFieldErrors( "name" ) ).thenReturn( true );
		validator.validate( publication, errors );

		InOrder sequence = inOrder( publicationRepository, errors );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( publicationRepository, never() ).findOne( Mockito.<Predicate>any() );
	}

	@Test
	public void nameMustBeUnique() {
		WebCmsPublication publication = WebCmsPublication.builder()
		                                                 .name( "publication name" )
		                                                 .build();
		when( publicationRepository.findOne( Mockito.<Predicate>any() ) ).thenReturn( Optional.of( publication ) );
		WebCmsPublication newPublication = WebCmsPublication.builder()
		                                                    .name( "publication name" )
		                                                    .build();

		validator.validate( newPublication, errors );
		InOrder sequence = inOrder( publicationRepository, errors );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( publicationRepository ).findOne( Mockito.<Predicate>any() );
		sequence.verify( errors ).rejectValue( "name", "alreadyExists" );
	}

	@Test
	public void existingErrorOnPublicationKey() {
		WebCmsPublication publication = new WebCmsPublication();
		publication.setName( "test" );

		when( errors.hasFieldErrors( "name" ) ).thenReturn( false );
		when( errors.hasFieldErrors( "publicationKey" ) ).thenReturn( true );
		validator.validate( publication, errors );

		InOrder sequence = inOrder( publicationRepository, errors );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( publicationRepository, times( 1 ) ).findOne( Mockito.<Predicate>any() );
		sequence.verify( errors, times( 2 ) ).hasFieldErrors( "publicationKey" );
		sequence.verify( publicationRepository, never() ).findOneByPublicationKeyAndDomain( "new-publication-key", null );
	}

	@Test
	public void publicationKeyMustBeUnique() {
		WebCmsPublication publication = WebCmsPublication.builder()
		                                                 .name( "publication name" )
		                                                 .publicationKey( "publication-key" )
		                                                 .build();
		when( publicationRepository.findOneByPublicationKeyAndDomain( "publication-key", null ) ).thenReturn( Optional.of( publication ) );
		WebCmsPublication newPublication = WebCmsPublication.builder()
		                                                    .publicationKey( "publication-key" )
		                                                    .name( "my name" )
		                                                    .build();

		validator.validate( newPublication, errors );
		InOrder sequence = inOrder( publicationRepository, errors );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( publicationRepository, times( 1 ) ).findOne( Mockito.<Predicate>any() );
		sequence.verify( publicationRepository, times( 1 ) ).findOneByPublicationKeyAndDomain( "publication-key", null );
		sequence.verify( errors, times( 1 ) ).rejectValue( "publicationKey", "alreadyExists" );
	}

	@Test
	public void objectIdMustBeUnique() {
		WebCmsPublication publication = WebCmsPublication.builder()
		                                                 .name( "publication name" )
		                                                 .publicationKey( "publication-key" )
		                                                 .objectId( "objectId" )
		                                                 .build();
		when( publicationRepository.findOneByObjectId( "wcm:asset:publication:objectId" ) ).thenReturn( Optional.of( publication ) );
		WebCmsPublication newPublication = WebCmsPublication.builder()
		                                                    .publicationKey( "new-publication-key" )
		                                                    .name( "my name" )
		                                                    .objectId( "objectId" )
		                                                    .build();

		validator.validate( newPublication, errors );
		InOrder sequence = inOrder( publicationRepository, errors );
		sequence.verify( errors ).hasFieldErrors( "name" );
		sequence.verify( publicationRepository, times( 1 ) ).findOne( Mockito.<Predicate>any() );
		sequence.verify( errors ).hasFieldErrors( "publicationKey" );
		sequence.verify( publicationRepository, times( 1 ) ).findOneByPublicationKeyAndDomain( "new-publication-key", null );
		sequence.verify( errors ).hasFieldErrors( "publicationKey" );
		sequence.verify( errors ).hasFieldErrors( "objectId" );
		sequence.verify( publicationRepository, times( 1 ) ).findOneByObjectId( "wcm:asset:publication:objectId" );
		sequence.verify( errors, times( 1 ) ).rejectValue( "objectId", "alreadyExists" );
		verifyNoMoreInteractions( errors );
	}
}
