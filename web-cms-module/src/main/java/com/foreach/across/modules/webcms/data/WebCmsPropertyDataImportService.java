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

import com.foreach.across.core.annotations.RefreshableCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dispatches to the appropriate {@link WebCmsPropertyDataImporter} for all custom properties.
 * A custom property is marked by a prefix eg. prefix "wcm" for property "wcm:components".
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Service
public final class WebCmsPropertyDataImportService
{
	private Collection<WebCmsPropertyDataImporter> propertyDataImporters = Collections.emptyList();

	@SuppressWarnings("unchecked")
	public boolean executeBeforeAssetSaved( WebCmsDataEntry assetData, Map<String, Object> propertiesData, Object asset, WebCmsDataAction action ) {
		return execute( WebCmsPropertyDataImporter.Phase.BEFORE_ASSET_SAVED, assetData, propertiesData, asset, action );
	}

	public boolean executeAfterAssetSaved( WebCmsDataEntry assetData, Map<String, Object> propertiesData, Object asset, WebCmsDataAction action ) {
		return execute( WebCmsPropertyDataImporter.Phase.AFTER_ASSET_SAVED, assetData, propertiesData, asset, action );
	}

	@SuppressWarnings("unchecked")
	private boolean execute( WebCmsPropertyDataImporter.Phase phase,
	                         WebCmsDataEntry assetData,
	                         Map<String, Object> propertiesData,
	                         Object asset,
	                         WebCmsDataAction action ) {
		return propertiesData
				.keySet()
				.stream()
				.flatMap(
						propertyName ->
								propertyDataImporters
										.stream()
										.filter(
												importer -> importer.getPhase() == phase
														&& importer.supports( assetData, propertyName, asset, action )
										)
										.map(
												importer -> importer.importData(
														assetData,
														new WebCmsDataEntry( propertyName, propertiesData.get( propertyName ) ),
														asset,
														action
												)
										)

				)
				.collect( Collectors.toSet() )
				.contains( Boolean.TRUE );
	}

	@Autowired
	void setPropertyDataImporters( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsPropertyDataImporter> propertyDataImporters ) {
		this.propertyDataImporters = propertyDataImporters;
	}
}
