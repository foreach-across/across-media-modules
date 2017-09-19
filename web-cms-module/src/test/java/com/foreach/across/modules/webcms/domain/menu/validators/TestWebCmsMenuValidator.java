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

package com.foreach.across.modules.webcms.domain.menu.validators;

import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsMenuValidator
{
	@Mock
	private WebCmsMenuRepository repository;

	@Mock
	private Errors errors;

	@InjectMocks
	private WebCmsMenuValidator validator;

	@Test
	public void nameMustBeUnique() {
		WebCmsMenu menu = WebCmsMenu.builder()
		                            .name( "my-name" )
		                            .build();
		when( repository.findOneByNameAndDomain( any(), any() ) ).thenReturn( menu );

		WebCmsMenu newMenu = WebCmsMenu.builder()
		                               .name( "my-name" )
		                               .build();

		validator.postValidation( newMenu, errors );

		verify( errors ).rejectValue( "name", "alreadyExists" );

	}

	@Test
	public void objectIdMustBeUnique() {
		WebCmsMenu menu = WebCmsMenu.builder()
		                            .name( "my-name" )
		                            .objectId( "wcm:menu:my-name" )
		                            .build();
		when( repository.findOneByObjectId( any() ) ).thenReturn( menu );

		WebCmsMenu newMenu = WebCmsMenu.builder()
		                               .name( "my-other-name" )
		                               .objectId( "wcm:menu:my-name" )
		                               .build();

		validator.postValidation( newMenu, errors );

		verify( errors ).rejectValue( "objectId", "alreadyExists" );
		verify( errors, never() ).rejectValue( "name", "alreadyExists" );
	}
}
