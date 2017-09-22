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
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
@RequiredArgsConstructor
@Component
public class MapToWebCmsPublicationConverter implements Converter<Map<String, Object>, WebCmsPublication>
{
	private final WebCmsPublicationRepository publicationRepository;
	private final WebCmsDataConversionService conversionService;
	private final WebCmsPublicationService publicationService;

	@Autowired
	void register( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( this );
	}

	@Override
	public WebCmsPublication convert( Map<String, Object> data ) {
		if ( data.containsKey( "objectId" ) ) {
			return publicationRepository.findOneByObjectId( Objects.toString( data.get( "objectId" ) ) );
		}

		if ( data.containsKey( "publicationKey" ) ) {
			String typeKey = Objects.toString( data.get( "publicationKey" ) );

			if ( data.containsKey( "domain" ) ) {
				return publicationService.getPublicationByKey( typeKey, conversionService.convert( data.get( "domain" ), WebCmsDomain.class ) );
			}

			return publicationService.getPublicationByKey( typeKey );
		}

		return null;
	}
}
