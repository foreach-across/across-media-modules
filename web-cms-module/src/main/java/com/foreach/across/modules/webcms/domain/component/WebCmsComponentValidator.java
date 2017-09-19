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

import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validates that a shared web component (no owner id) has a title and a unique name.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsComponentValidator extends EntityValidatorSupport<WebCmsComponent>
{
	private final WebCmsComponentRepository componentRepository;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsComponent.class.isAssignableFrom( clazz );
	}

	@Override
	protected void preValidation( WebCmsComponent entity, Errors errors, Object... validationHints ) {
		if ( StringUtils.isBlank( entity.getName() ) && !StringUtils.isBlank( entity.getTitle() ) ) {
			entity.setName( WebCmsUtils.generateUrlPathSegment( entity.getTitle() ) );
		}
		else if ( !StringUtils.isBlank( entity.getName() ) && StringUtils.isBlank( entity.getTitle() ) ) {
			entity.setTitle( EntityUtils.generateDisplayName( entity.getName() ) );
		}
	}

	@Override
	protected void postValidation( WebCmsComponent entity, Errors errors, Object... validationHints ) {
		if ( !errors.hasFieldErrors( "name" ) ) {
			if ( !entity.hasOwner() || !StringUtils.isEmpty( entity.getName() ) ) {
				WebCmsComponent existing = componentRepository.findOneByOwnerObjectIdAndNameAndDomain( entity.getOwnerObjectId(), entity.getName(),
				                                                                                       entity.getDomain() );

				if ( existing != null && !entity.equals( existing ) ) {
					errors.rejectValue( "name", "alreadyExists" );
				}
			}
		}
	}
}
