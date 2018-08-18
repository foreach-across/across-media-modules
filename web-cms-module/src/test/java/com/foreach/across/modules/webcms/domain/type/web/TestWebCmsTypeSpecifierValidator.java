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

package com.foreach.across.modules.webcms.domain.type.web;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import com.querydsl.core.types.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsTypeSpecifierValidator
{
	@Mock
	private WebCmsTypeSpecifierRepository repository;

	@Mock
	private Errors errors;

	@InjectMocks
	private WebCmsTypeSpecifierValidator validator;

	@Test
	public void nameMustBeUnique() {
		WebCmsTypeSpecifier articleType = WebCmsArticleType.builder()
		                                                   .name( "Test article type" )
		                                                   .typeKey( "test-article-type" )
		                                                   .build();
		when( repository.findOne( Mockito.<Predicate>any() ) ).thenReturn( Optional.of( articleType ) );
		WebCmsTypeSpecifier newArticleType = WebCmsArticleType.builder()
		                                                      .name( "Test article type" )
		                                                      .typeKey( "another-test-article-type" )
		                                                      .build();

		validator.postValidation( newArticleType, errors );

		verify( errors ).rejectValue( "name", "alreadyExists" );
	}

	@Test
	public void typeKeyMustBeUniqueWithinNoDomain() {
		WebCmsTypeSpecifier articleType = WebCmsArticleType.builder()
		                                                   .name( "My first article type" )
		                                                   .typeKey( "test-article-type" )
		                                                   .build();
		when( repository.findOneByObjectTypeAndTypeKeyAndDomain( any(), any(), eq( null ) ) ).thenReturn( articleType );
		WebCmsTypeSpecifier newArticleType = WebCmsArticleType.builder()
		                                                      .name( "My second article type" )
		                                                      .typeKey( "test-article-type" )
		                                                      .build();

		validator.postValidation( newArticleType, errors );

		verify( errors ).rejectValue( "typeKey", "alreadyExists" );
	}

	@Test
	public void typeKeyMustBeUniqueWithinDomain() {
		WebCmsDomain domain = WebCmsDomain.builder().domainKey( "my-domain" ).build();
		WebCmsTypeSpecifier articleType = WebCmsArticleType.builder()
		                                                   .name( "My first article type" )
		                                                   .typeKey( "test-article-type" )
		                                                   .domain( domain )
		                                                   .build();
		when( repository.findOneByObjectTypeAndTypeKeyAndDomain( any(), any(), eq( domain ) ) ).thenReturn( articleType );
		WebCmsTypeSpecifier newArticleType = WebCmsArticleType.builder()
		                                                      .name( "My second article type" )
		                                                      .typeKey( "test-article-type" )
		                                                      .domain( domain )
		                                                      .build();

		validator.postValidation( newArticleType, errors );

		verify( errors ).rejectValue( "typeKey", "alreadyExists" );
	}

	@Test
	public void objectIdMustBeUnique() {
		WebCmsTypeSpecifier articleType = WebCmsArticleType.builder()
		                                                   .name( "My first article type" )
		                                                   .typeKey( "first-test-article-type" )
		                                                   .objectId( "wcm:type:test-article-type" )
		                                                   .build();
		when( repository.findOneByObjectId( articleType.getObjectId() ) ).thenReturn( articleType );
		WebCmsArticleType newArticleType = WebCmsArticleType.builder()
		                                                    .name( "My second article type" )
		                                                    .typeKey( "second-test-article-type" )
		                                                    .objectId( "wcm:type:test-article-type" )
		                                                    .build();

		validator.postValidation( newArticleType, errors );

		verify( errors ).rejectValue( "objectId", "alreadyExists" );
		verify( errors, never() ).rejectValue( "name", "alreadyExists" );
		verify( errors, never() ).rejectValue( "typeKey", "alreadyExists" );
		verify( errors, never() ).rejectValue( "typeKey", "may not contain a colon if not attached to a domain" );
		verify( errors, never() ).rejectValue( "typeKey", "must be prefixed by domainKey of attached domain followed by a colon: " + articleType.getTypeKey() );
	}
}
