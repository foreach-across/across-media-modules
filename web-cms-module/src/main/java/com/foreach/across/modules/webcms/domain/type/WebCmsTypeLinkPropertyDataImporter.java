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

import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsPropertyDataImporter;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Supports assets or type specifiers to have the <strong>wcm:types</strong> property,
 * create links to the types specified.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public class WebCmsTypeLinkPropertyDataImporter implements WebCmsPropertyDataImporter<WebCmsObject>
{
	public static final String PROPERTY_NAME = "wcm:types";

	private final WebCmsDataConversionService conversionService;
	private final WebCmsTypeSpecifierLinkRepository typeLinkRepository;

	@Override
	public boolean supports( Phase phase,
	                         WebCmsDataEntry dataEntry, Object asset,
	                         WebCmsDataAction action ) {
		return Phase.AFTER_ASSET_SAVED.equals( phase ) && PROPERTY_NAME.equals( dataEntry.getParentKey() ) && asset instanceof WebCmsObject;
	}

	@Override
	public boolean importData( Phase phase,
	                           WebCmsDataEntry propertyData,
	                           WebCmsObject asset,
	                           WebCmsDataAction action ) {
		WebCmsTypeSpecifierLink typeLink = new WebCmsTypeSpecifierLink();
		conversionService.convertToPropertyValues( propertyData.getMapData(), typeLink );
		typeLink.setOwner( asset );

		Optional<WebCmsTypeSpecifierLink> existing = typeLinkRepository.findOneByOwnerObjectIdAndLinkTypeAndTypeSpecifier(
				typeLink.getOwnerObjectId(),
				typeLink.getLinkType(),
				typeLink.getTypeSpecifier()
		);

		if ( !existing.isPresent() ) {
			typeLinkRepository.save( typeLink );
		}

		return true;
	}
}
