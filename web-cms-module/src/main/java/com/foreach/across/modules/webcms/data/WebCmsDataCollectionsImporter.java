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

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Provides a default callback for {@link WebCmsDataEntry} imports.
 */
@RequiredArgsConstructor
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebCmsDataCollectionsImporter implements WebCmsDataImporter
{
	private final WebCmsDataImportService dataImportService;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return !data.isSingleValue();
	}

	@Override
	public void importData( WebCmsDataEntry data ) {
		if ( data.isMapData() ) {
			data.getMapData().forEach( ( key, properties ) -> dataImportService.importData( WebCmsDataEntry.builder()
			                                                                                               .key( key )
			                                                                                               .parent( data )
			                                                                                               .data( properties )
			                                                                                               .build() ) );
		}
		else {
			data.getCollectionData().forEach( properties -> dataImportService.importData( WebCmsDataEntry.builder()
			                                                                                             .parent( data )
			                                                                                             .data( properties )
			                                                                                             .build() ) );
		}
	}
}
