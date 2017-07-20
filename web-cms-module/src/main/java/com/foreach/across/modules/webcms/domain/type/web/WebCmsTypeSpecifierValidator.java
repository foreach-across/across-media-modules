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

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.type.QWebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator for typeKey and objectId of {@link WebCmsTypeSpecifier}.
 * Can serve as a base class.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebCmsTypeSpecifierValidator extends EntityValidatorSupport<WebCmsTypeSpecifier>
{
	private WebCmsTypeSpecifierRepository typeSpecifierRepository;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsTypeSpecifier.class.isAssignableFrom( clazz );
	}

	@Override
	protected void postValidation( WebCmsTypeSpecifier entity, Errors errors, Object... validationHints ) {
		if ( !errors.hasFieldErrors( "name" ) ) {
			QWebCmsTypeSpecifier query = QWebCmsTypeSpecifier.webCmsTypeSpecifier;
			WebCmsTypeSpecifier existing = typeSpecifierRepository.findOne( query.name.equalsIgnoreCase( entity.getName() )
			                                                                          .and( query.objectType.eq( entity.getObjectType() ) ) );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "name", "alreadyExists" );
			}
		}

		if ( !errors.hasFieldErrors( "typeKey" ) ) {
			WebCmsTypeSpecifier existing = typeSpecifierRepository.findOneByObjectTypeAndTypeKey( entity.getObjectType(), entity.getTypeKey() );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "typeKey", "alreadyExists" );
			}
		}

		if ( !errors.hasFieldErrors( "typeKey" ) && !errors.hasFieldErrors( "objectId" ) ) {
			WebCmsTypeSpecifier existing = typeSpecifierRepository.findOneByObjectId( entity.getObjectId() );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "objectId", "alreadyExists" );
			}
		}
	}

	@Autowired
	public void setTypeSpecifierRepository( WebCmsTypeSpecifierRepository typeSpecifierRepository ) {
		this.typeSpecifierRepository = typeSpecifierRepository;
	}
}
