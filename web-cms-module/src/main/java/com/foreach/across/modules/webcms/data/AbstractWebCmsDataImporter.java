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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.foreach.across.modules.webcms.data.WebCmsDataAction.*;

/**
 * Abstract base class for a {@link WebCmsDataImporter} that needs to support both map and
 * collection data, as well as the different {@link WebCmsDataImportAction} options.
 * <p/>
 * Each entry to import is expected to represent a single object.
 * Dispatches to the {@link WebCmsPropertyDataImportService} for custom property handling.
 * If neither related property handlers have imported anything, nor values on the original
 * asset have been changed, the asset will not be updated.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
public abstract class AbstractWebCmsDataImporter<T, U> implements WebCmsDataImporter
{
	protected final Logger LOG = LoggerFactory.getLogger( getClass() );

	private WebCmsDataConversionService conversionService;
	private WebCmsPropertyDataImportService propertyDataImportService;

	@Override
	public final void importData( WebCmsDataEntry data ) {
		if ( data.isMapData() ) {
			data.getMapData().forEach( ( key, properties ) -> importSingleEntry( new WebCmsDataEntry( key, data, properties ) ) );
		}
		else {
			data.getCollectionData().forEach( properties -> importSingleEntry( new WebCmsDataEntry( null, data, properties ) ) );
		}
	}

	/**
	 * Import a single data entry from the original data set.
	 * Depending on the original data set the single entry will have a key set or not.
	 *
	 * @param data to import
	 */
	final void importSingleEntry( WebCmsDataEntry data ) {
		LOG.trace( "Importing data entry {}", data );

		T existing = retrieveExistingInstance( data );
		WebCmsDataAction action = resolveAction( data.getImportAction(), existing, data );
		LOG.trace( "Resolved import action {} to {}, existing item: {}", data.getImportAction(), action, existing != null );

		if ( action != null ) {
			if ( action == DELETE ) {
				deleteInstance( existing, data );
			}
			else {
				U dto = createDto( data, existing, action );

				if ( dto != null ) {
					boolean dataValuesApplied = applyDataValues( data.getMapData(), dto );
					boolean customPropertyDataApplied = propertyDataImportService.executeBeforeAssetSaved( data, data.getMapData(), dto, action );

					if ( existing == null || dataValuesApplied || customPropertyDataApplied ) {
						saveDto( dto, action, data );
					}
					else {
						LOG.trace( "Skipping saving DTO as no actual values have been updated" );
					}

					propertyDataImportService.executeAfterAssetSaved( data, data.getMapData(), dto, action );
				}
			}
		}
		else {
			LOG.trace( "Skipping data entry as no valid action was resolved" );
		}
	}

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

	/**
	 * Resolve the import action into the actual action to perform depending if there's an
	 * existing item we're dealing with or not.  If this method returns {@code null} the
	 * processing of the data entry will be skipped.
	 *
	 * @param requested import action
	 * @param existing  item or {@code null} if none
	 * @param data      set of data being passed
	 * @return action to perform or {@code null} if none
	 */
	protected WebCmsDataAction resolveAction( WebCmsDataImportAction requested, T existing, WebCmsDataEntry data ) {
		if ( existing != null ) {
			if ( requested == WebCmsDataImportAction.DELETE ) {
				return DELETE;
			}
			if ( requested == WebCmsDataImportAction.CREATE_OR_UPDATE || requested == WebCmsDataImportAction.UPDATE ) {
				return UPDATE;
			}
			if ( requested == WebCmsDataImportAction.CREATE_OR_REPLACE || requested == WebCmsDataImportAction.REPLACE ) {
				return REPLACE;
			}
		}
		else if ( requested == WebCmsDataImportAction.CREATE
				|| requested == WebCmsDataImportAction.CREATE_OR_UPDATE
				|| requested == WebCmsDataImportAction.CREATE_OR_REPLACE ) {
			return CREATE;
		}

		return null;
	}

	/**
	 * Get the existing object that this data represents.
	 * If no instance exists, this method should return {@code null}..
	 *
	 * @param data entry
	 * @return instance or {@code null}
	 */
	protected abstract T retrieveExistingInstance( WebCmsDataEntry data );

	/**
	 * Create a DTO object for either a new instance or an existing instance.
	 * If the existing parameter is {@code null} a new instance should be created.
	 * <p/>
	 * Note that the DTO should not apply the data entry values yet, but the data entry
	 * can be used to determine the initial type of instance that needs to be created.
	 * <p/>
	 * If the DTO is null, the import will be skipped but properties will still be called.
	 *
	 * @param data     entry
	 * @param existing instance or {@code null} if a new instance should be created
	 * @param action   purpose for which the DTO should be created (create, delete or replace)
	 * @return DTO
	 */
	protected abstract U createDto( WebCmsDataEntry data, T existing, WebCmsDataAction action );

	/**
	 * Perform the delete action on existing instance.
	 *
	 * @param instance to delete
	 * @param data     entry that is being imported
	 */
	protected abstract void deleteInstance( T instance, WebCmsDataEntry data );

	/**
	 * Save an updated or created instance.
	 *
	 * @param dto    that should be saved
	 * @param action type of save action (create, update or replace)
	 * @param data   entry that is being imported
	 */
	protected abstract void saveDto( U dto, WebCmsDataAction action, WebCmsDataEntry data );

	@Autowired
	void setConversionService( WebCmsDataConversionService conversionService ) {
		this.conversionService = conversionService;
	}

	@Autowired
	void setPropertyDataImportService( WebCmsPropertyDataImportService propertyDataImportService ) {
		this.propertyDataImportService = propertyDataImportService;
	}
}
