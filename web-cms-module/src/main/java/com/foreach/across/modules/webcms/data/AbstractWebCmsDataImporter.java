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

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.infrastructure.ValidationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.foreach.across.modules.webcms.data.WebCmsDataAction.DELETE;
import static com.foreach.across.modules.webcms.infrastructure.WebCmsUtils.convertImportActionToDataAction;

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

	private MessageSource messageSource;
	private WebCmsDataConversionService conversionService;
	private WebCmsMultiDomainService multiDomainService;
	private WebCmsPropertyDataImportService propertyDataImportService;

	/**
	 * Import a single data entry from the original data set.
	 * Depending on the original data set the single entry will have a key set or not.
	 * If the given data entry results in a new {@link WebCmsDomainBound} object,
	 * the domain will be prefilled with {@link WebCmsMultiDomainService#getCurrentDomain()}
	 *
	 * @param data to import
	 */
	@Override
	public final void importData( WebCmsDataEntry data ) {
		try {
			LOG.trace( "Importing data entry {} {}", data.getImportAction(), data );

			T existing = retrieveExistingInstance( data );
			WebCmsDataAction action = resolveAction( data.getImportAction(), existing );
			LOG.trace( "Resolved import action {} to {}, existing item: {}", data.getImportAction(), action, existing != null );

			if ( action != null ) {
				if ( action == DELETE ) {
					deleteInstance( existing, data );
				}
				else {
					Map<String, Object> dataValues = new LinkedHashMap<>( data.getMapData() );
					U dto = createDto( data, existing, action, dataValues );

					if ( existing == null && dto instanceof WebCmsDomainBound ) {
						( (WebCmsDomainBound) dto ).setDomain( multiDomainService.getCurrentDomain() );
					}

					if ( dto != null ) {
						boolean dataValuesApplied = data.isSingleValue() ? applySingleValue( data.getSingleValue(), dto ) : applyDataValues( dataValues, dto );
						boolean customPropertyDataApplied = propertyDataImportService.executeBeforeAssetSaved( data, dataValues, dto, action );

						if ( existing == null || dataValuesApplied || customPropertyDataApplied ) {
							save( dto, action, data );
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
		catch ( WebCmsDataImportException die ) {
			throw die;
		}
		catch ( Exception e ) {
			throw new WebCmsDataImportException( data, e );
		}
	}

	/**
	 * Retrieves the current domain for the specified data entry and class.
	 * If the data entry contains a key, that domain will be returned.
	 * If the data entry does not contain a key, the domain will be retrieved using
	 * {@link WebCmsMultiDomainService#getCurrentDomainForType(Class)}
	 *
	 * @param item       the data entry
	 * @param entityType to check domain support
	 * @return the domain to which the entity should be attached
	 */
	protected WebCmsDomain retrieveDomainForDataEntry( WebCmsDataEntry item, Class<?> entityType ) {
		return item.getMapData().containsKey( "domain" )
				? conversionService.convert( item.getMapData().get( "domain" ), WebCmsDomain.class )
				: multiDomainService.getCurrentDomainForType( entityType );
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
	 * Apply the data value to the dto object.
	 * If this method returns {@code false} no values have been applied to the DTO and actual updating might get skipped.
	 *
	 * @param value to apply
	 * @param dto   to set the value on
	 * @return true if the DTO has been modified
	 */
	private boolean applySingleValue( Object value, U dto ) {
		return conversionService.convertSingleValue( value, dto );
	}

	/**
	 * Resolve the import action into the actual action to perform depending if there's an
	 * existing item we're dealing with or not.  If this method returns {@code null} the
	 * processing of the data entry will be skipped.
	 *
	 * @param requested import action
	 * @param existing  item or {@code null} if none
	 * @return action to perform or {@code null} if none
	 */
	protected WebCmsDataAction resolveAction( WebCmsDataImportAction requested, T existing ) {
		return convertImportActionToDataAction( existing, requested );
	}

	private void save( U dto, WebCmsDataAction action, WebCmsDataEntry data ) {
		U itemToSave = prepareForSaving( dto, data );

		if ( itemToSave != null ) {
			validateForSaving( itemToSave );
			saveDto( itemToSave, action, data );
		}
		else {
			LOG.trace( "Skipping data import {} import for {} - prepareForSaving returned null", data );
		}
	}

	/**
	 * This performs validation by building an {@link Errors} object, and dispatching to the {@link #validate(T, Errors)}
	 * method.  If actual validation errors are added, this will result in a validation exception.
	 *
	 * @param itemToSave item to be saved
	 */
	protected final void validateForSaving( U itemToSave ) {
		if ( itemToSave != null ) {
			Errors errors = new BeanPropertyBindingResult( itemToSave, itemToSave.getClass().getSimpleName() );
			validate( itemToSave, errors );

			if ( errors.hasErrors() ) {
				throw new ValidationFailedException( messageSource, "Validation failed for " + itemToSave.getClass(), errors );
			}
		}
		else {
			throw new IllegalArgumentException( "Attempting to validate null object" );
		}
	}

	/**
	 * Override if you want to post process an item before saving.
	 * Useful if you want to generate property values for example.
	 *
	 * @param itemToBeSaved original item to be saved
	 * @param data          that was used to build the item
	 * @return new item to be saved instead - null if saving should be skipped
	 */
	protected U prepareForSaving( U itemToBeSaved, WebCmsDataEntry data ) {
		return itemToBeSaved;
	}

	/**
	 * Perform some custom validation on the item to be saved.
	 * If any errors are added to the collection, this will result in a validation exception being thrown.
	 *
	 * @param itemToBeSaved item to be saved
	 * @param errors        collection to add validation errors to
	 */
	protected void validate( U itemToBeSaved, Errors errors ) {
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
	 * @param data       entry
	 * @param existing   instance or {@code null} if a new instance should be created
	 * @param action     purpose for which the DTO should be created (create, delete or replace)
	 * @param dataValues modifiable collection of the data values that will be applied afterwards
	 * @return DTO
	 */
	protected abstract U createDto( WebCmsDataEntry data, T existing, WebCmsDataAction action, Map<String, Object> dataValues );

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

	/**
	 * Add force update property to a data values map.  This will cause the item to be updated,
	 * even if nothing else has changed.  Mainly used in a REPLACE use case.
	 *
	 * @param dataValues map
	 */
	protected final void addForceUpdateProperty( Map<String, Object> dataValues ) {
		dataValues.put( WebCmsForceUpdatePropertyImporter.FORCE_UPDATE, true );
	}

	@Autowired
	void setMessageSource( MessageSource messageSource ) {
		this.messageSource = messageSource;
	}

	@Autowired
	void setConversionService( WebCmsDataConversionService conversionService ) {
		this.conversionService = conversionService;
	}

	@Autowired
	void setMultiDomainService( WebCmsMultiDomainService multiDomainService ) {
		this.multiDomainService = multiDomainService;
	}

	@Autowired
	public void setPropertyDataImportService( WebCmsPropertyDataImportService propertyDataImportService ) {
		this.propertyDataImportService = propertyDataImportService;
	}
}
