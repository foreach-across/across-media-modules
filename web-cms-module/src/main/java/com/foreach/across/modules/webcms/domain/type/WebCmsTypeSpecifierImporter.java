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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsPropertyDataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Slf4j
@RequiredArgsConstructor
final class WebCmsTypeSpecifierImporter implements WebCmsDataImporter
{
	private final WebCmsTypeRegistry typeRegistry;
	private final WebCmsTypeSpecifierRepository typeRepository;
	private final WebCmsDataConversionService conversionService;
	private final WebCmsPropertyDataImportService propertyDataImportService;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return "types".equals( data.getParentKey() );
	}

	@Override
	public final void importData( WebCmsDataEntry data ) {
		data.getMapData().forEach( ( key, properties ) -> importSingleAsset( new WebCmsDataEntry( key, data.getKey(), properties ) ) );
	}

	private void importSingleAsset( WebCmsDataEntry item ) {
		String typeGroup = item.getParentKey();
		val implementationType = typeRegistry.retrieveTypeSpecifierClass( item.getParentKey() )
		                                     .orElseThrow( () -> new IllegalArgumentException( "Unable to import type: " + item.getParentKey() ) );

		WebCmsTypeSpecifier existing = retrieveExistingType( typeGroup, (String) item.getMapData().get( "objectId" ), item.getKey() );
		WebCmsTypeSpecifier dto = createDto( existing, implementationType );

		if ( dto != null ) {
			LOG.trace( "{} WebCmsTypeSpecifier {} with objectId {}", dto.isNew() ? "Creating" : "Updating", typeGroup, dto.getObjectId() );

			if ( propertyDataImportService.executeBeforeAssetSaved( item, item.getMapData(), dto ) ) {
				LOG.trace( "WebCmsTypeSpecifier {} with objectId {}: custom properties have been imported before asset saved", typeGroup, dto.getObjectId() );
			}

			boolean isModified = conversionService.convertToPropertyValues( item.getMapData(), dto );

			if ( isModified || dto.isNew() ) {
				WebCmsTypeSpecifier itemToSave = prepareForSaving( dto, item );

				if ( itemToSave != null ) {
					LOG.debug( "Saving WebCmsTypeSpecifier {} with objectId {} (insert: {}) - {}",
					           typeGroup, itemToSave.getObjectId(), dto.isNew(), dto );
					typeRepository.save( itemToSave );
				}
				else {
					LOG.trace( "Skipping WebCmsTypeSpecifier {} import for objectId {} - prepareForSaving returned null", typeGroup, dto.getObjectId() );
				}
			}
			else {
				LOG.trace( "Skipping WebCmsTypeSpecifier {} import for objectId {} - nothing modified", typeGroup, dto.getObjectId() );
			}

			if ( propertyDataImportService.executeAfterAssetSaved( item, item.getMapData(), dto ) ) {
				LOG.trace( "WebCmsTypeSpecifier {} with objectId {}: custom properties have been imported after asset saved", typeGroup, dto.getObjectId() );
			}
		}
		else {
			LOG.trace( "Skipping WebCmsTypeSpecifier {} import for entry {} - no DTO was created", typeGroup, item.getKey() );
		}
	}

	protected WebCmsTypeSpecifier prepareForSaving( WebCmsTypeSpecifier itemToBeSaved, WebCmsDataEntry data ) {
		if ( itemToBeSaved.isNew() ) {
			if ( itemToBeSaved.getTypeKey() == null ) {
				itemToBeSaved.setTypeKey( data.getKey() );
			}
			if ( !data.getMapData().containsKey( "objectId" ) ) {
				itemToBeSaved.setObjectId( itemToBeSaved.getTypeKey() );
			}
		}
		return itemToBeSaved;
	}

	private WebCmsTypeSpecifier createDto( WebCmsTypeSpecifier<?> existing, Class<? extends WebCmsTypeSpecifier> implementationType ) {
		if ( existing != null ) {
			return existing.toDto();
		}

		val supplier = typeRegistry.retrieveSupplier( implementationType )
		                           .orElseThrow( () -> new IllegalStateException( "No valid supplier was registered for: " + implementationType ) );
		return supplier.get();
	}

	private WebCmsTypeSpecifier retrieveExistingType( String typeGroup, String objectId, String typeKey ) {
		WebCmsTypeSpecifier existing = null;

		if ( objectId != null ) {
			existing = typeRepository.findOneByObjectId( objectId );
		}

		return existing != null ? existing : typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, typeKey );
	}
}
