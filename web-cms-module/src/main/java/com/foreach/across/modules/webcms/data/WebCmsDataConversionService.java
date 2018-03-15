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

package com.foreach.across.modules.webcms.data;

import com.foreach.across.core.convert.StringToDateTimeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Specific {@link org.springframework.core.convert.ConversionService} used by the {@link WebCmsDataImportServiceImpl}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Slf4j
@Service("webCmsDataConversionService")
public class WebCmsDataConversionService extends DefaultConversionService
{
	private final Map<Class, String> singleValueProperties = new HashMap<>();

	public WebCmsDataConversionService() {
		addConverter( new StringToDateTimeConverter( this ) );
	}

	/**
	 * Registers a property name for a class to use during a single value import.
	 * If a property name has already been registered for the class, the new property will override the existing one.
	 *
	 * @param owner        the owner of the property
	 * @param propertyName of the property
	 */
	public void registerSingleValueProperty( Class owner, String propertyName ) {
		singleValueProperties.put( owner, propertyName );
	}

	/**
	 * Convert raw data to property values on a DTO object.  Will convert the separate values
	 * using the converters registered and will only modify the DTO for those properties where the current value
	 * is different from the new.
	 *
	 * @param data map with the raw values
	 * @param dto  to set te properties on
	 * @return true if properties have been set (values were different)
	 */
	@SuppressWarnings("unchecked")
	public boolean convertToPropertyValues( Map<String, Object> data, Object dto ) {
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl( dto );

		AtomicBoolean modified = new AtomicBoolean( false );

		data.forEach( ( propertyName, propertyValue ) -> {
			if ( propertyName.contains( ":" ) || propertyName.startsWith( "#" ) ) {
				LOG.trace( "Skipping property {} - assuming separate importer wil be used", propertyName );
			}
			else {
				TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor( propertyName );

				if ( typeDescriptor == null ) {
					LOG.warn( "Skipping unknown property: {}", propertyName );
				}
				else {
					TypeDescriptor sourceType = TypeDescriptor.forObject( propertyValue );
					if ( propertyValue instanceof Map && !canConvert( sourceType, typeDescriptor ) ) {
						Object target = beanWrapper.getPropertyValue( propertyName );

						if ( target == null ) {
							LOG.error( "Unable to convert property {} - target is null", propertyName );
							throw new RuntimeException( "Unable to converted nested object - value is null for property " + propertyName );
						}

						if ( convertToPropertyValues( (Map<String, Object>) propertyValue, target ) ) {
							modified.set( true );
						}
					}
					else {
						Object valueToSet = convert( propertyValue, sourceType, typeDescriptor );

						if ( propertyValue != null && valueToSet == null ) {
							throw new IllegalArgumentException(
									"Illegal converted value for '" + propertyName + "': " +
											sourceType.getName() + " to " + typeDescriptor.getName() + " resulted in null for '" + propertyValue + "'" );
						}

						Object currentValue = beanWrapper.getPropertyValue( propertyName );

						if ( !Objects.equals( currentValue, valueToSet ) ) {
							modified.set( true );
							beanWrapper.setPropertyValue( propertyName, valueToSet );
						}
					}
				}
			}
		} );

		return modified.get();
	}

	/**
	 * Converts a single raw value to a property on a DTO object.  Will convert the value
	 * using the converters registered and will only modify the DTO for the property where the value
	 * is different from the new.
	 * <p/>
	 * The property to which the single value should be converted should have been registered using {@link #registerSingleValueProperty(Class, String)}.
	 *
	 * @param value to set
	 * @param dto   to set the value on
	 * @return true if a property has been set (values were different)
	 */
	public boolean convertSingleValue( Object value, Object dto ) {
		String propertyName = singleValueProperties.get( dto.getClass() );
		if ( propertyName == null ) {
			throw new IllegalArgumentException( "Unable to convert value '" + value + "' as single value for " + dto.getClass().getName() );
		}
		return convertToPropertyValues( Collections.singletonMap( propertyName, value ), dto );
	}
}
