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

package com.foreach.across.modules.webcms.domain.publication;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public final class StringToWebCmsPublicationConverter implements Converter<String, WebCmsPublication>
{
	private final WebCmsPublicationRepository publicationRepository;
	private final WebCmsMultiDomainService multiDomainService;

	@Autowired
	void register( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( this );
	}

	@Override
	public WebCmsPublication convert( String source ) {
		if ( NumberUtils.isDigits( source ) ) {
			return publicationRepository.findOne( Long.parseLong( source ) );
		}
		else if ( WebCmsUtils.isObjectIdForCollection( source, WebCmsPublication.COLLECTION_ID ) ) {
			return publicationRepository.findOneByObjectId( source );
		}
		return publicationRepository.findOneByPublicationKeyAndDomain( source, multiDomainService.getCurrentDomainForType( WebCmsPublication.class ) );
	}
}

