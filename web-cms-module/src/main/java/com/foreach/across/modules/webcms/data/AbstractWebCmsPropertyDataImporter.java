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

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.foreach.across.modules.webcms.data.WebCmsDataAction.DELETE;
import static com.foreach.across.modules.webcms.data.WebCmsDataActionResolver.resolveAction;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebCmsPropertyDataImporter<T, U extends SettableIdBasedEntity<U>> implements WebCmsPropertyDataImporter<T>
{
	private WebCmsDataConversionService conversionService;

	@Override
	public boolean importData( Phase phase,
	                           WebCmsDataEntry data,
	                           T parent,
	                           WebCmsDataAction action ) {
		try {
			LOG.trace( "Importing data entry {}", data );

			U existing = getExisting( data, parent );
			action = resolveAction( existing, data );
			LOG.trace( "Resolved import action {} to {}, existing item: {}", data.getImportAction(), action, existing != null );

			if ( action != null ) {
				if ( action == DELETE ) {
					delete( existing );
				}
				else {
					U dto = createDto( data, existing, action, parent );

					if ( dto != null ) {
						boolean dataValuesApplied = applyDataValues( data.getMapData(), dto );
						if ( existing == null || dataValuesApplied ) {
							save( dto );
						}
						else {
							LOG.trace( "Skipping saving DTO as no actual values have been updated" );
						}
					}
				}
			}
			else {
				LOG.trace( "Skipping data entry as no valid action was resolved" );
			}
		}
		catch ( WebCmsDataImportException die ) {
			throw die;
		}
		catch ( Exception e ) {
			throw new WebCmsDataImportException( data, e );
		}
		return true;
	}

	protected abstract U createDto( WebCmsDataEntry menuDataSet, U existing, WebCmsDataAction action, T parent );

	protected abstract void save( U dto );

	protected abstract void delete( U dto );

	protected abstract U getExisting( WebCmsDataEntry dataKey, T parent );

	/**
	 * Apply the data values to the dto object.  The default implementation assumes that the values
	 * map with actual dto class properties.
	 * <p/>
	 * If this method returns {@code false} no values have been applied to the DTO and actual updating
	 * might get skipped.
	 *
	 * @param values to apply (key/value pairs)
	 * @param dto    to set the values on
	 * @return true if the DTO has been modified
	 */
	protected boolean applyDataValues( Map<String, Object> values, U dto ) {
		return conversionService.convertToPropertyValues( values, dto );
	}

	@Autowired
	void setConversionService( WebCmsDataConversionService conversionService ) {
		this.conversionService = conversionService;
	}
}
