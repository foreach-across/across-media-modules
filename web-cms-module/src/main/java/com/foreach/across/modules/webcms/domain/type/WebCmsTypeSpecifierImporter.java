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

import com.foreach.across.modules.webcms.data.AbstractWebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Slf4j
@RequiredArgsConstructor
final class WebCmsTypeSpecifierImporter extends AbstractWebCmsDataImporter<WebCmsTypeSpecifier<?>, WebCmsTypeSpecifier<?>>
{
	private final WebCmsTypeRegistry typeRegistry;
	private final WebCmsTypeSpecifierRepository typeRepository;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return "types".equals( data.getParentKey() );
	}

	@Override
	protected WebCmsTypeSpecifier retrieveExistingInstance( WebCmsDataEntry data ) {
		String typeGroup = StringUtils.defaultString( data.getParentKey(), (String) data.getMapData().get( "typeGroup" ) );
		String typeKey = data.getKey();
		String objectId = (String) data.getMapData().get( "objectId" );

		WebCmsTypeSpecifier existing = null;

		if ( objectId != null ) {
			existing = typeRepository.findOneByObjectId( objectId );
		}

		return existing != null ? existing : typeRepository.findOneByObjectTypeAndTypeKey( typeGroup, typeKey );
	}

	@Override
	protected WebCmsTypeSpecifier createDto( WebCmsDataEntry data, WebCmsTypeSpecifier<?> existing, WebCmsDataAction action ) {
		String typeGroup = StringUtils.defaultString( data.getParentKey(), (String) data.getMapData().get( "typeGroup" ) );
		val implementationType = typeRegistry.retrieveTypeSpecifierClass( typeGroup )
		                                     .orElseThrow( () -> new IllegalArgumentException( "Unable to import type: " + typeGroup ) );

		if ( existing != null && action != WebCmsDataAction.REPLACE ) {
			return existing.toDto();
		}

		val supplier = typeRegistry.retrieveSupplier( implementationType )
		                           .orElseThrow( () -> new IllegalStateException( "No valid supplier was registered for: " + implementationType ) );

		if ( existing != null ) {
			// replace action
			WebCmsTypeSpecifier<?> type = supplier.get();
			type.setId( existing.getId() );
			type.setCreatedBy( existing.getCreatedBy() );
			type.setCreatedDate( existing.getCreatedDate() );

			return type;
		}

		return supplier.get();
	}

	@Override
	protected void deleteInstance( WebCmsTypeSpecifier instance, WebCmsDataEntry data ) {
		typeRepository.delete( instance );
	}

	@Override
	protected void saveDto( WebCmsTypeSpecifier dto, WebCmsDataAction action, WebCmsDataEntry data ) {
		if ( dto.isNew() ) {
			if ( dto.getTypeKey() == null ) {
				dto.setTypeKey( data.getKey() );
			}
			if ( !data.getMapData().containsKey( "objectId" ) ) {
				dto.setObjectId( dto.getTypeKey() );
			}
		}

		LOG.debug( "Saving WebCmsTypeSpecifier {} with objectId {} (insert: {}) - {}",
		           dto.getClass().getSimpleName(), dto.getObjectId(), dto.isNew(), dto );
		typeRepository.save( dto );

	}
}
