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
 * API for a single importer component that supports a particular {@link WebCmsDataEntry}.
 * The central access point is the {@link WebCmsDataImportService} that will iterate through all
 * {@link WebCmsDataImporter} beans and use {@link #supports(WebCmsDataEntry)} to determine if
 * it should be used for handling that data type.
 * <p/>
 * In most cases you want to extend {@link AbstractWebCmsDataImporter} which supports both map and list
 * sub-type data and calls different sub-methods depending on the {@link WebCmsDataImportAction}.
 *
 * @author Arne Vandamme
 * @see WebCmsDataImportService
 * @see AbstractWebCmsDataImporter
 * @since 0.0.1
 */
public interface WebCmsDataImporter
{
	boolean supports( WebCmsDataEntry data );

	void importData( WebCmsDataEntry data );
}
