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

		WebCmsTypeSpecifier existing = retrieveExistingType( typeGroup, (String) item.getMapData().get( "uniqueKey" ), item.getKey() );
		WebCmsTypeSpecifier dto = createDto( existing, implementationType );

		if ( dto != null ) {
			LOG.trace( "{} WebCmsTypeSpecifier {} with uniqueKey {}", dto.isNew() ? "Creating" : "Updating" );

			boolean isModified = conversionService.convertToPropertyValues( item.getMapData(), dto );

			if ( isModified ) {
				WebCmsTypeSpecifier itemToSave = prepareForSaving( dto, item );

				if ( itemToSave != null ) {
					LOG.debug( "Saving WebCmsTypeSpecifier {} with uniqueKey {} (insert: {}) - {}",
					           typeGroup, itemToSave.getUniqueKey(), dto.isNew(), dto );
					typeRepository.save( itemToSave );
				}
				else {
					LOG.trace( "Skipping WebCmsTypeSpecifier {} import for uniqueKey {} - prepareForSaving returned null", typeGroup, dto.getUniqueKey() );
				}
			}
			else {
				LOG.trace( "Skipping WebCmsTypeSpecifier {} import for uniqueKey {} - nothing modified", typeGroup, dto.getUniqueKey() );
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
			if ( !data.getMapData().containsKey( "uniqueKey" ) ) {
				itemToBeSaved.setUniqueKey( itemToBeSaved.getTypeKey() );
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

	private WebCmsTypeSpecifier retrieveExistingType( String typeGroup, String uniqueKey, String typeKey ) {
		WebCmsTypeSpecifier existing = null;

		if ( uniqueKey != null ) {
			existing = typeRepository.findOneByUniqueKey( uniqueKey );
		}

		return existing != null ? existing : typeRepository.findOneByTypeGroupAndTypeKey( typeGroup, typeKey );
	}
}
