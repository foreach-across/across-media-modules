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
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
class StringToCollectionOrMapConverter implements GenericConverter
{
	private final WebCmsDataObjectMapper dataObjectMapper;

	@Autowired
	void registerOnDataConversionService( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( this );
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		HashSet<ConvertiblePair> pairs = new HashSet<>();
		pairs.add( new ConvertiblePair( String.class, Collection.class ) );
		pairs.add( new ConvertiblePair( String.class, Map.class ) );
		return pairs;
	}

	@Override
	public Object convert( Object source, TypeDescriptor sourceType, TypeDescriptor targetType ) {
		String json = (String) source;
		return dataObjectMapper.readFromString( json, targetType );
	}
}
