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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link String} to {@link WebCmsDomain}.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
public class StringToWebCmsDomainConverter implements Converter<String, WebCmsDomain>
{
	private final WebCmsDomainService domainService;
	private final WebCmsDomainRepository domainRepository;

	@Autowired
	void registerToConversionService( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( this );
	}

	@Autowired
	void registerToMvcConversionService( @Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN) ConverterRegistry mvcConversionService ) {
		mvcConversionService.addConverter( this );
	}

	@Override
	public WebCmsDomain convert( String value ) {
		if ( NumberUtils.isDigits( value ) ) {
			return domainRepository.findOne( Long.parseLong( value ) );
		}
		else if ( WebCmsUtils.isObjectIdForCollection( value, WebCmsDomain.COLLECTION_ID ) ) {
			return domainService.getDomain( value );
		}
		else if ( !StringUtils.isEmpty( value ) ) {
			return domainService.getDomainByKey( value );
		}
		return null;
	}
}
