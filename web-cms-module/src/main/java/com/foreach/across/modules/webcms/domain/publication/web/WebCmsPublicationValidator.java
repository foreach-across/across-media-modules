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

package com.foreach.across.modules.webcms.domain.publication.web;

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.domain.publication.QWebCmsPublication;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator for publicationKey and objectId of {@link com.foreach.across.modules.webcms.domain.publication.WebCmsPublication}.
 * Can serve as a base class.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public final class WebCmsPublicationValidator extends EntityValidatorSupport<WebCmsPublication>
{
	private final WebCmsPublicationRepository publicationRepository;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsPublication.class.isAssignableFrom( clazz );
	}

	@Override
	protected void postValidation( WebCmsPublication entity, Errors errors, Object... validationHints ) {
		if ( !errors.hasFieldErrors( "name" ) ) {
			QWebCmsPublication query = QWebCmsPublication.webCmsPublication;
			BooleanBuilder builder = new BooleanBuilder();
			if ( entity.getDomain() != null ) {
				builder.and( query.domain.eq( entity.getDomain() ) );
			}

			publicationRepository.findOne( builder.and( query.name.equalsIgnoreCase( entity.getName() ) ) )
			                     .filter( existing -> !entity.equals( existing ) )
			                     .ifPresent( e -> errors.rejectValue( "name", "alreadyExists" ) );
		}

		if ( !errors.hasFieldErrors( "publicationKey" ) ) {
			publicationRepository.findOneByPublicationKeyAndDomain( entity.getPublicationKey(), entity.getDomain() )
			                     .filter( existing -> !entity.equals( existing ) )
			                     .ifPresent( e -> errors.rejectValue( "publicationKey", "alreadyExists" ) );
		}

		if ( !errors.hasFieldErrors( "publicationKey" ) && !errors.hasFieldErrors( "objectId" ) ) {
			publicationRepository.findOneByObjectId( entity.getObjectId() )
			                     .filter( existing -> !entity.equals( existing ) )
			                     .ifPresent( e -> errors.rejectValue( "objectId", "alreadyExists" ) );
		}
	}
}
