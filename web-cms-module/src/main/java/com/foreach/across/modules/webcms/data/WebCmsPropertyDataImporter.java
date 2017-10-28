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

/**
 * Importer interface for a custom data property (eg. "wcm:components").
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public interface WebCmsPropertyDataImporter<T>
{
	enum Phase
	{
		/**
		 * Import this data before the parent asset has been saved.
		 */
		BEFORE_ASSET_SAVED,

		/**
		 * Import this data after the parent asset has been saved.
		 * Required for relationships to the parent.
		 */
		AFTER_ASSET_SAVED
	}

	/**
	 * @param phase     in which this importer should execute
	 * @param dataEntry property data to set
	 * @param asset     asset being imported
	 * @param action    that will be performed with the asset   @return true if this importer should be executed
	 */
	boolean supports( Phase phase,
	                  WebCmsDataEntry dataEntry, Object asset,
	                  WebCmsDataAction action );

	/**
	 * Perform the actual import of the property data.
	 * If this method returns {@code true} this means that data has been imported.
	 *
	 * @param phase        in which this importer should execute
	 * @param propertyData specific property data set
	 * @param asset        asset being imported
	 * @param action       that will be performed with the asset
	 * @return true if anything has been imported
	 */
	boolean importData( Phase phase,
	                    WebCmsDataEntry propertyData,
	                    T asset,
	                    WebCmsDataAction action );
}
