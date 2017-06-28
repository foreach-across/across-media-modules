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

import java.util.Map;

/**
 * Core API for importing data represented as a map into known assets.
 *
 * @author Arne Vandamme
 * @see WebCmsDataImportServiceImpl
 * @since 0.0.2
 */
public interface WebCmsDataImportService
{
	/**
	 * Import a collection of data.  The data is expected to be represented as a map containing other maps.
	 * The initial keys in the data map should be the root types.
	 *
	 * @param data containing everything that should be imported
	 */
	void importData( Map<String, Object> data );

	/**
	 * Import a data entry.  All registered {@link WebCmsDataImporter} beans will be checked to see if
	 * they support the given data set.
	 *
	 * @param data containing everything that should be imported
	 */
	void importData( WebCmsDataEntry data );
}
