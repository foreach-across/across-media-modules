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

package com.foreach.across.modules.webcms.domain.domain.validators;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestWebCmsDomainValidator
{
	@Mock
	private WebCmsDomainRepository repository;

	@Mock
	private Errors errors;

	@InjectMocks
	private WebCmsDomainValidator validator;

	@Test
	public void nameMustBeUnique() {
		WebCmsDomain domain = WebCmsDomain.builder()
		                                  .name( "Test Domain" )
		                                  .domainKey( "test-domain" )
		                                  .build();
		when( repository.findOne( Mockito.<Predicate>any() ) ).thenReturn( Optional.of( domain ) );
		WebCmsDomain newDomain = WebCmsDomain.builder()
		                                     .name( "Test Domain" )
		                                     .domainKey( "another-key" )
		                                     .build();

		validator.postValidation( newDomain, errors );

		verify( errors ).rejectValue( "name", "alreadyExists" );
	}

	@Test
	public void domainKeyMustBeUnique() {
		WebCmsDomain domain = WebCmsDomain.builder()
		                                  .name( "Test Domain" )
		                                  .domainKey( "test-domain" )
		                                  .build();
		when( repository.findOneByDomainKey( domain.getDomainKey() ) ).thenReturn( Optional.of( domain ) );
		WebCmsDomain newDomain = WebCmsDomain.builder()
		                                     .name( "Another Domain" )
		                                     .domainKey( "test-domain" )
		                                     .build();

		validator.postValidation( newDomain, errors );

		verify( errors ).rejectValue( "domainKey", "alreadyExists" );
	}

	@Test
	public void objectIdMustBeUnique() {
		WebCmsDomain domain = WebCmsDomain.builder()
		                                  .name( "Test Domain" )
		                                  .domainKey( "test-domain" )
		                                  .objectId( "wcm:domain:test-domain" )
		                                  .build();
		when( repository.findOneByObjectId( domain.getObjectId() ) ).thenReturn( Optional.of( domain ) );
		WebCmsDomain newDomain = WebCmsDomain.builder()
		                                     .name( "Another Domain" )
		                                     .domainKey( "another-domain" )
		                                     .objectId( "wcm:domain:test-domain" )
		                                     .build();

		validator.postValidation( newDomain, errors );

		verify( errors ).rejectValue( "objectId", "alreadyExists" );
		verify( errors, never() ).rejectValue( "domainKey", "alreadyExists" );
		verify( errors, never() ).rejectValue( "name", "alreadyExists" );
	}
}
