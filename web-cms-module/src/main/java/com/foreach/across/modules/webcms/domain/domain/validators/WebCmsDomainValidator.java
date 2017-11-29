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

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.domain.domain.QWebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator for domainKey and name of {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomain}.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
public class WebCmsDomainValidator extends EntityValidatorSupport<WebCmsDomain>
{
	private final WebCmsDomainRepository domainRepository;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsDomain.class.isAssignableFrom( clazz );
	}

	@Override
	protected void postValidation( WebCmsDomain entity, Errors errors, Object... validationHints ) {
		if ( !errors.hasFieldErrors( "name" ) ) {
			val query = QWebCmsDomain.webCmsDomain;
			WebCmsDomain existing = domainRepository.findOne( query.name.equalsIgnoreCase( entity.getName() ) );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "name", "alreadyExists" );
			}
		}

		if ( !errors.hasFieldErrors( "domainKey" ) ) {
			WebCmsDomain existing = domainRepository.findOneByDomainKey( entity.getDomainKey() );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "domainKey", "alreadyExists" );
			}
		}

		if ( !errors.hasFieldErrors( "domainKey" ) && !errors.hasFieldErrors( "objectId" ) ) {
			WebCmsDomain existing = domainRepository.findOneByObjectId( entity.getObjectId() );
			if ( existing != null && !entity.equals( existing ) ) {
				errors.rejectValue( "objectId", "alreadyExists" );
			}
		}
	}
}
