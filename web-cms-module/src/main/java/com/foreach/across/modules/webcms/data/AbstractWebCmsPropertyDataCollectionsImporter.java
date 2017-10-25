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

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Base class that redispatches incoming property data imports that could not be resolved.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
public abstract class AbstractWebCmsPropertyDataCollectionsImporter implements WebCmsPropertyDataImporter
{
	@Autowired
	private WebCmsPropertyDataImportService propertyDataImportService;

	@Override
	public boolean importData( Phase phase, WebCmsDataEntry dataEntry, Object asset, WebCmsDataAction action ) {
		if ( dataEntry.isMapData() ) {
			return dataEntry.getMapData().entrySet().stream()
			                .map( entry ->
					                      propertyDataImportService.importData( phase,
					                                                            WebCmsDataEntry.builder()
					                                                                           .key( entry.getKey() )
					                                                                           .importAction( WebCmsDataImportAction.CREATE_OR_UPDATE )
					                                                                           .parent( dataEntry )
					                                                                           .data( entry.getValue() == null ? new HashMap<>() : entry
							                                                                           .getValue() )
					                                                                           .build(),
					                                                            asset,
					                                                            action )
			                )
			                .collect( Collectors.toList() )
			                .contains( Boolean.TRUE );
		}
		else {
			return dataEntry.getCollectionData().stream()
			                .map( properties -> propertyDataImportService.importData( phase,
			                                                                          WebCmsDataEntry.builder()
			                                                                                         .parent( dataEntry )
			                                                                                         .importAction(
					                                                                                         WebCmsDataImportAction.CREATE_OR_UPDATE )
			                                                                                         .data( properties )
			                                                                                         .build(),
			                                                                          asset,
			                                                                          action ) )
			                .collect( Collectors.toList() )
			                .contains( Boolean.TRUE );
		}
	}
}
