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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Specific {@link org.springframework.core.convert.ConversionService} used by the {@link WebCmsDataImportService}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Slf4j
@Service("webCmsDataConversionService")
public class WebCmsDataConversionService extends DefaultConversionService
{
	/**
	 * Convert raw data to property values on a DTO object.  Will convert the separate values
	 * using the converters registered and will only modify the DTO for those properties where the current value
	 * is different from the new.
	 *
	 * @param data map with the raw values
	 * @param dto  to set te properties on
	 * @return true if properties have been set
	 */
	public boolean convertToPropertyValues( Map<String, Object> data, Persistable dto ) {
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl( dto );

		AtomicBoolean modified = new AtomicBoolean( false );

		data.forEach( ( propertyName, propertyValue ) -> {
			if ( propertyName.contains( ":" ) ) {
				LOG.trace( "Skipping property {} - assuming separate importer wil be used", propertyName );
			}
			else {
				TypeDescriptor typeDescriptor = beanWrapper.getPropertyTypeDescriptor( propertyName );

				if ( typeDescriptor == null ) {
					throw new IllegalArgumentException( "Unknown property: " + propertyName );
				}

				Object valueToSet = convert( propertyValue, TypeDescriptor.forObject( propertyValue ), typeDescriptor );
				Object currentValue = beanWrapper.getPropertyValue( propertyName );

				if ( dto.isNew() || !Objects.equals( currentValue, valueToSet ) ) {
					modified.set( true );
					beanWrapper.setPropertyValue( propertyName, valueToSet );
				}
			}
		} );

		return modified.get();
	}
}
