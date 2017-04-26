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

package com.foreach.across.modules.webcms.domain.component;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsPropertyDataImportService;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Base class for importing a simple asset type.  An asset has a specific data key (in the asset collection)
 * and a class extending {@link WebCmsComponent}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Scope("prototype")
public class WebCmsComponentImporter implements WebCmsDataImporter
{
	protected final Logger LOG = LoggerFactory.getLogger( getClass() );

	private WebCmsComponentRepository componentRepository;
	private WebCmsDataConversionService conversionService;
	private WebCmsPropertyDataImportService propertyDataImportService;

	private final String dataKey = "component";

	private WebCmsObject owner;

	@Override
	public final boolean supports( WebCmsDataEntry data ) {
		return "assets".equals( data.getParentKey() ) && dataKey.equals( data.getKey() );
	}

	@Override
	public final void importData( WebCmsDataEntry data ) {
		if ( data.isMapData() ) {
			data.getMapData().forEach( ( key, properties ) -> importSingleComponent( new WebCmsDataEntry( key, data.getKey(), properties ) ) );
		}
		else {
			data.getCollectionData().forEach( properties -> importSingleComponent( new WebCmsDataEntry( null, data.getKey(), properties ) ) );
		}
	}

	private void importSingleComponent( WebCmsDataEntry item ) {
		WebCmsComponent existing = retrieveExistingAsset( (String) item.getMapData().get( "objectId" ), item.getKey() );

		String action = (String) item.getMapData().get( "wcm:action" );

		if ( action != null ) {
			if ( "delete".equals( action ) && existing != null ) {
				LOG.trace( "WebCmsComponent {} with objectId {}: removing component", dataKey, existing.getObjectId() );
				componentRepository.delete( existing );
				return;
			}
		}

		WebCmsComponent dto = createDto( existing );

		if ( dto != null ) {
			if ( propertyDataImportService.executeBeforeAssetSaved( item, item.getMapData(), dto ) ) {
				LOG.trace( "WebCmsComponent {} with objectId {}: custom properties have been imported before asset saved", dataKey, dto.getObjectId() );
			}

			LOG.trace( "{} WebCmsComponent {} with objectId {}", dto.isNew() ? "Creating" : "Updating", dataKey, dto.getObjectId() );

			boolean isModified = conversionService.convertToPropertyValues( item.getMapData(), dto );

			if ( isModified ) {
				WebCmsComponent itemToSave = prepareForSaving( dto, item );

				if ( itemToSave != null ) {
					LOG.debug( "Saving WebCmsComponent {} with objectId {} (insert: {}) - {}",
					           dataKey, itemToSave.getObjectId(), dto.isNew(), dto );
					componentRepository.save( itemToSave );
				}
				else {
					LOG.trace( "Skipping WebCmsComponent {} import for objectId {} - prepareForSaving returned null", dataKey, dto.getObjectId() );
				}
			}
			else {
				LOG.trace( "Skipping WebCmsComponent {} import for objectId {} - nothing modified", dataKey, dto.getObjectId() );
			}

			if ( propertyDataImportService.executeAfterAssetSaved( item, item.getMapData(), dto ) ) {
				LOG.trace( "WebCmsComponent {} with objectId {}: custom properties have been imported after asset saved", dataKey, dto.getObjectId() );
			}
		}
		else {
			LOG.trace( "Skipping WebCmsComponent {} import for entry {} - no DTO was created", dataKey, item.getKey() );
		}
	}

	private WebCmsComponent retrieveExistingAsset( String objectId, String entryKey ) {
		WebCmsComponent existing = null;

		if ( objectId != null ) {
			existing = componentRepository.findOneByObjectId( objectId );
		}

		if ( existing == null ) {
			existing = componentRepository.findOneByOwnerObjectIdAndName( getOwnerObjectId(), entryKey );
		}

		return existing;
	}

	/**
	 * Override if you want to post process an item before saving.
	 * Useful if you want to generate property values for example.
	 *
	 * @param itemToBeSaved original item to be saved
	 * @param data          that was used to build the item
	 * @return new item to be saved instead - null if saving should be skipped
	 */
	private WebCmsComponent prepareForSaving( WebCmsComponent itemToBeSaved, WebCmsDataEntry data ) {
		if ( itemToBeSaved.getName() == null ) {
			itemToBeSaved.setName( data.getKey() );
		}
		return itemToBeSaved;
	}

	private WebCmsComponent createDto( WebCmsComponent itemToUpdate ) {
		return itemToUpdate != null ? itemToUpdate.toDto() : WebCmsComponent.builder().ownerObjectId( getOwnerObjectId() ).build();
	}

	private String getOwnerObjectId() {
		return owner != null ? owner.getObjectId() : null;
	}

	/**
	 * Set the owner of the objects being created.
	 */
	public void setOwner( WebCmsObject owner ) {
		this.owner = owner;
	}

	@Autowired
	void setComponentRepository( WebCmsComponentRepository componentRepository ) {
		this.componentRepository = componentRepository;
	}

	@Autowired
	void setConversionService( WebCmsDataConversionService conversionService ) {
		this.conversionService = conversionService;
	}

	@Autowired
	void setPropertyDataImportService( WebCmsPropertyDataImportService propertyDataImportService ) {
		this.propertyDataImportService = propertyDataImportService;
	}
}
