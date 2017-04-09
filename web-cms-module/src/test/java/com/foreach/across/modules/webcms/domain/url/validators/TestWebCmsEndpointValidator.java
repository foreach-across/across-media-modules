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

package com.foreach.across.modules.webcms.domain.url.validators;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsEndpointValidator
{
	@Mock
	private WebCmsUrlRepository repository;

	@Mock
	private Errors errors;

	@InjectMocks
	private WebCmsUrlValidator validator;

	@Test
	public void supportsEndpoint() throws Exception {
		assertTrue( validator.supports( WebCmsUrl.class ) );
	}

	@Test
	public void multiplePrimariesNotAllowed() throws Exception {
		WebCmsEndpoint endpoint = WebCmsAssetEndpoint.builder()
		                                             .build();
		when( repository.findAllByEndpoint( endpoint ) ).thenReturn( Arrays.asList(
				WebCmsUrl.builder().id( 1L ).path( "/ok" ).httpStatus( HttpStatus.OK ).primary( true ).build(),
				WebCmsUrl.builder().id( 2L ).path( "/no-content" ).httpStatus( HttpStatus.NO_CONTENT ).primary( true ).build(),
				WebCmsUrl.builder().id( 3L ).httpStatus( HttpStatus.NOT_FOUND ).primary( false ).build()
		) );
		WebCmsUrl url = WebCmsUrl.builder().primary( true ).endpoint( endpoint ).build();
		when( errors.hasFieldErrors( "endpoint" ) ).thenReturn( false );

		validator.preValidation( url, errors );

		verify( errors ).rejectValue( "primary", "onlyOnePrimaryUrlPerEndpoint", new Object[] { "/ok" }, "Another primary URL exists." );
	}

	@Test
	public void dontErrorWhenAlreadyRejected() throws Exception {
		WebCmsEndpoint endpoint = WebCmsAssetEndpoint.builder()
		                                             .build();
		WebCmsUrl url = WebCmsUrl.builder().endpoint( endpoint ).build();
		when( errors.hasFieldErrors( "endpoint" ) ).thenReturn( true );

		validator.preValidation( url, errors );

		verify( errors, never() ).rejectValue( "endpoint", "onlyOnePrimaryUrlPerEndpoint" );
	}

	@Test
	public void dontErrorWhenOnlyOnePrimary() throws Exception {
		WebCmsEndpoint endpoint = WebCmsAssetEndpoint.builder()
		                                             .build();
		when( repository.findAllByEndpoint( endpoint ) ).thenReturn( Arrays.asList(
				WebCmsUrl.builder().httpStatus( HttpStatus.OK ).build(),
				WebCmsUrl.builder().httpStatus( HttpStatus.NOT_FOUND ).build()
		) );
		WebCmsUrl url = WebCmsUrl.builder().endpoint( endpoint ).build();
		when( errors.hasFieldErrors( "endpoint" ) ).thenReturn( true );

		validator.preValidation( url, errors );

		verify( errors, never() ).rejectValue( "endpoint", "onlyOnePrimaryUrlPerEndpoint" );
	}

	@Test
	public void dontErrorWhenNoPrimary() throws Exception {
		WebCmsEndpoint endpoint = WebCmsAssetEndpoint.builder()
		                                             .build();
		when( repository.findAllByEndpoint( endpoint ) ).thenReturn( Arrays.asList(
				WebCmsUrl.builder().httpStatus( HttpStatus.NOT_MODIFIED ).build(),
				WebCmsUrl.builder().httpStatus( HttpStatus.NOT_FOUND ).build()
		) );
		WebCmsUrl url = WebCmsUrl.builder().endpoint( endpoint ).build();
		when( errors.hasFieldErrors( "endpoint" ) ).thenReturn( true );

		validator.preValidation( url, errors );

		verify( errors, never() ).rejectValue( "endpoint", "onlyOnePrimaryUrlPerEndpoint" );
	}

}