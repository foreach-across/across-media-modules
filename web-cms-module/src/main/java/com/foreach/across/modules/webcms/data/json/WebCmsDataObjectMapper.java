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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Locale;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;

/**
 * Wraps around an {@link ObjectMapper} for JSON (de-)serialization.
 * Customized configuration for data importing and exporting.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Service
@Slf4j
public final class WebCmsDataObjectMapper
{
	private final ObjectMapper objectMapper;
	private final TypeFactory typeFactory;

	public WebCmsDataObjectMapper( WebCmsObjectSerializer cmsObjectSerializer, WebCmsObjectDeserializers cmsObjectDeserializers ) {
		SimpleModule jsonModule = new SimpleModule( WebCmsModule.NAME );
		jsonModule.addSerializer( WebCmsObject.class, cmsObjectSerializer );
		jsonModule.setDeserializers( cmsObjectDeserializers );

		objectMapper = Jackson2ObjectMapperBuilder.json()
		                                          .locale( Locale.US )
		                                          .indentOutput( false )
		                                          .modules( jsonModule )
		                                          .failOnUnknownProperties( false )
		                                          .failOnEmptyBeans( false )
		                                          .featuresToEnable(
				                                          READ_UNKNOWN_ENUM_VALUES_AS_NULL,
				                                          READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
		                                          )
		                                          .build();
		typeFactory = objectMapper.getTypeFactory();
	}

	public String writeToString( Object object ) {
		try {
			return objectMapper.writeValueAsString( object );
		}
		catch ( IOException ioe ) {
			throw new RuntimeException( ioe );
		}
	}

	/**
	 * Update an object with properties defined in a JSON string.
	 *
	 * @param json        containing the values
	 * @param target      object to update (should not be null)
	 * @param failOnError true if an exception should be thrown if an error occurs
	 * @return true if parsing was complete, false if an error has occurred
	 */
	public boolean updateFromString( String json, Object target, boolean failOnError ) {
		try {
			objectMapper.readerForUpdating( target ).readValue( json );
			return true;
		}
		catch ( IOException ioe ) {
			LOG.error( "Exception updating object from JSON - failing: {}", failOnError, ioe );
			if ( failOnError ) {
				throw new RuntimeException( ioe );
			}
		}

		return false;
	}

	/**
	 * Deserialize a string to the output type.
	 *
	 * @param json      data as string
	 * @param valueType to create
	 * @param <T>       value type reference
	 * @return instance
	 */
	public <T> T readFromString( String json, Class<T> valueType ) {
		return readFromString( json, TypeDescriptor.valueOf( valueType ) );
	}

	/**
	 * Deserialize a string to the output type.
	 *
	 * @param json           data as string
	 * @param typeDescriptor representing the output type to create
	 * @param <T>            output type reference
	 * @return instance
	 */
	@SuppressWarnings("all")
	@SneakyThrows
	public <T> T readFromString( String json, TypeDescriptor typeDescriptor ) {
		return (T) objectMapper.readValue( json, convertResolvableTypeToJavaType( typeDescriptor.getResolvableType() ) );
	}

	private JavaType convertResolvableTypeToJavaType( ResolvableType resolvableType ) {
		return typeFactory.constructSimpleType( resolvableType.resolve(), convertResolvableTypeToJavaType( resolvableType.getGenerics() ) );
	}

	private JavaType[] convertResolvableTypeToJavaType( ResolvableType[] resolvableTypes ) {
		JavaType[] javaTypes = new JavaType[resolvableTypes.length];

		for ( int i = 0; i < resolvableTypes.length; i++ ) {
			javaTypes[i] = convertResolvableTypeToJavaType( resolvableTypes[i] );
		}

		return javaTypes;
	}
}
