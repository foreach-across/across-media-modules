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

import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsPropertyDataImporter;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelAllowsSingleValueImports;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Supports assets or type specifiers to have the <strong>wcm:components</strong> property,
 * imports all components specified that way.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsComponentPropertyDataImporter implements WebCmsPropertyDataImporter<WebCmsObject>
{
	public static final String PROPERTY_NAME = "wcm:components";

	private final BeanFactory beanFactory;

	@Override
	public Phase getPhase() {
		return Phase.AFTER_ASSET_SAVED;
	}

	@Override
	public boolean supports( WebCmsDataEntry parentData, String propertyName, Object asset, WebCmsDataAction action ) {
		return PROPERTY_NAME.equals( propertyName ) && asset instanceof WebCmsObject;
	}

	@Override
	public boolean importData( WebCmsDataEntry parentData, WebCmsDataEntry propertyData, WebCmsObject asset, WebCmsDataAction action ) {
		WebCmsComponentImporter componentImporter = beanFactory.getBean( WebCmsComponentImporter.class );
		componentImporter.setOwner( asset );

		if ( propertyData.isMapData() ) {
			propertyData.getMapData().forEach(
					( key, value ) -> {
						WebCmsDataEntry entry = new WebCmsDataEntry( propertyData.getIdentifier(), key, value );
						if ( entry.isSingleValue() ) {
							WebCmsDataEntry temp = resolveSingleValueEntry( entry, asset );
							if ( temp != null ) {
								entry = temp;
							}
						}
						componentImporter.importData( entry );
					} );
					( key, value ) -> componentImporter
							.importData( WebCmsDataEntry.builder()
							                            .identifier( propertyData.getIdentifier() )
							                            .propertyDataName( PROPERTY_NAME )
							                            .key( key )
							                            .parent( parentData )
							                            .data( value )
							                            .build() ) );
		}
		else {
			propertyData.getCollectionData().forEach(
					properties -> componentImporter.importData( WebCmsDataEntry.builder()
					                                                           .propertyDataName( PROPERTY_NAME )
					                                                           .parent( propertyData )
					                                                           .data( properties )
					                                                           .build() ) );
		}

		return true;
	}

	private WebCmsDataEntry resolveSingleValueEntry( WebCmsDataEntry data, WebCmsObject asset ) {
		WebCmsComponentModelAllowsSingleValueImports model = null;

		if ( asset instanceof ContainerWebCmsComponentModel ) {
			WebCmsComponentModel member = ( (ContainerWebCmsComponentModel) asset ).getMember( data.getKey() );
			if ( member instanceof WebCmsComponentModelAllowsSingleValueImports ) {
				model = (WebCmsComponentModelAllowsSingleValueImports) member;
			}
		}
		else if ( asset instanceof WebCmsComponentModelAllowsSingleValueImports ) {
			model = (WebCmsComponentModelAllowsSingleValueImports) asset;
		}

		if ( model != null ) {
			Map<String, Object> map = new HashMap<>();
			map.put( model.getPropertyName(), data.getSingleValue() );
			return new WebCmsDataEntry( data.getIdentifier(), data.getKey(), map );
		}
		return null;
	}

}
