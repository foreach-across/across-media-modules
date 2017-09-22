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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Service
@RequiredArgsConstructor
class WebCmsMenuServiceImpl implements WebCmsMenuService
{
	private final WebCmsMultiDomainService multiDomainService;
	private final WebCmsMenuRepository menuRepository;

	@Override
	public WebCmsMenu getMenuByName( String menuName ) {
		return getMenuByName( menuName, multiDomainService.getCurrentDomainForType( WebCmsMenu.class ) );
	}

	@Override
	public WebCmsMenu getMenuByName( String menuName, WebCmsDomain domain ) {
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( menuName, domain );

		if ( menu == null && !WebCmsDomain.isNoDomain( domain ) && multiDomainService.isNoDomainAllowed( WebCmsMenu.class ) ) {
			menu = menuRepository.findOneByNameAndDomain( menuName, WebCmsDomain.NONE );
		}

		return menu;
	}
}
