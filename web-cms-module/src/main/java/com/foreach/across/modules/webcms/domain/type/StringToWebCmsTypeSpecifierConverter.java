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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link String} to {@link WebCmsTypeSpecifier}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class StringToWebCmsTypeSpecifierConverter implements ConverterFactory<String, WebCmsTypeSpecifier>
{
	private final WebCmsTypeSpecifierRepository typeSpecifierRepository;
	private final WebCmsTypeSpecifierService typeSpecifierService;

	@Autowired
	public void registerToConversionService( WebCmsDataConversionService conversionService ) {
		conversionService.addConverterFactory( this );
	}

	@Override
	public <T extends WebCmsTypeSpecifier> Converter<String, T> getConverter( Class<T> targetType ) {
		return ( id ) -> {
			WebCmsTypeSpecifier type = typeSpecifierRepository.findOneByObjectId( id ).orElse( null );

			if ( type == null ) {
				return typeSpecifierService.getTypeSpecifierByKey( id, targetType );
			}

			return targetType.cast( type );
		};
	}
}
