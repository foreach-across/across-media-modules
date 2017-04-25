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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Locale;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
public final class WebCmsDataObjectMapper
{
	private final ObjectMapper objectMapper;

	public WebCmsDataObjectMapper( WebCmsObjectSerializer cmsObjectSerializer,
	                               WebCmsObjectDeserializer cmsObjectDeserializer ) {
		objectMapper = Jackson2ObjectMapperBuilder.json()
		                                          .serializerByType( WebCmsObject.class, cmsObjectSerializer )
		                                          .deserializerByType( WebCmsObject.class, cmsObjectDeserializer )
		                                          .locale( Locale.US )
		                                          .indentOutput( false )
		                                          .build();
	}

	public String writeToString( Object object ) {
		try {
			return objectMapper.writeValueAsString( object );
		}
		catch ( IOException ioe ) {
			throw new RuntimeException( ioe );
		}
	}

	public <T> T readFromString( String json, Class<T> valueType ) {
		try {
			return objectMapper.readValue( json, valueType );
		}
		catch ( IOException ioe ) {
			throw new RuntimeException( ioe );
		}
	}

	public <T> T readFromString( String json, TypeReference<T> valueType ) {
		try {
			return objectMapper.readValue( json, valueType );
		}
		catch ( IOException ioe ) {
			throw new RuntimeException( ioe );
		}
	}
}
