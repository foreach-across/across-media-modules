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

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator for name of {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenu}.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@RequiredArgsConstructor
@Component
public class WebCmsMenuValidator extends EntityValidatorSupport<WebCmsMenu>
{
	private final WebCmsMenuRepository menuRepository;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsMenu.class.isAssignableFrom( clazz );
	}

	@Override
	protected void postValidation( WebCmsMenu entity, Errors errors, Object... validationHints ) {
		if ( !errors.hasFieldErrors( "name" ) ) {
			WebCmsMenu existing = menuRepository.findOneByNameAndDomain( entity.getName(), entity.getDomain() );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "name", "alreadyExists" );
			}
		}

		if ( !errors.hasFieldErrors( "name" ) && !errors.hasFieldErrors( "objectId" ) ) {
			WebCmsMenu existing = menuRepository.findOneByObjectId( entity.getObjectId() );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "objectId", "alreadyExists" );
			}
		}
	}
}
