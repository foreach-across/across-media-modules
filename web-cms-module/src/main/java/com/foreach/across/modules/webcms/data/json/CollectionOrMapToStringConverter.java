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

package com.foreach.across.modules.webcms.data.json;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * Replaces the {@link org.springframework.core.convert.support.CollectionToStringConverter}
 * in the {@link com.foreach.across.modules.webcms.data.WebCmsDataImportService}.  Uses the
 * {@link WebCmsDataObjectMapper} for collection conversion instead.
 * <p/>
 * Converts both {@link Collection} and {@link java.util.Map} to {@link String}.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
class CollectionOrMapToStringConverter implements Converter<Object, String>
{
	private final WebCmsDataObjectMapper dataObjectMapper;

	@Autowired
	void registerOnDataConversionService( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( Collection.class, String.class, this );
		conversionService.addConverter( Map.class, String.class, this );
	}

	@Override
	public String convert( Object source ) {
		return dataObjectMapper.writeToString( source );
	}
}
