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

package com.foreach.across.modules.webcms.domain.menu;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@ExtendWith(MockitoExtension.class)
public class TestWebCmsMenuService
{
	private WebCmsDomain domain = WebCmsDomain.builder().id( 123L ).build();

	@Mock
	private WebCmsMenuRepository menuRepository;

	@Mock
	private WebCmsMultiDomainService multiDomainService;

	@InjectMocks
	private WebCmsMenuServiceImpl menuService;

	@Test
	public void menuWithNameUsesTheCurrentDomainIfDomainBound() {
		WebCmsMenu noDomain = WebCmsMenu.builder().id( 1L ).build();
		WebCmsMenu withDomain = WebCmsMenu.builder().id( 2L ).build();

		when( menuRepository.findOneByNameAndDomain( "myMenu", WebCmsDomain.NONE ) ).thenReturn( Optional.of( noDomain ) );
		when( menuRepository.findOneByNameAndDomain( "myMenu", domain ) ).thenReturn( Optional.of( withDomain ) );

		assertSame( noDomain, menuService.getMenuByName( "myMenu" ) );
		verify( multiDomainService ).getCurrentDomainForType( WebCmsMenu.class );

		when( multiDomainService.getCurrentDomainForType( WebCmsMenu.class ) ).thenReturn( domain );
		assertSame( withDomain, menuService.getMenuByName( "myMenu" ) );
	}
}
