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

package com.foreach.across.modules.webcms.domain.page;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsDataImportException;
import com.foreach.across.modules.webcms.data.WebCmsPropertyDataImportService;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetRepository;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionException;

import java.util.Collections;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsPageImporter
{
	@Mock
	private WebCmsAssetRepository assetRepository;

	@Mock
	private WebCmsDataConversionService conversionService;

	@Mock
	private WebCmsPageService pageService;

	private WebCmsPropertyDataImportService propertyDataImportService = new WebCmsPropertyDataImportService();

	@InjectMocks
	private WebCmsPageImporter pageImporter;

	@Before
	public void setUp() throws Exception {
		pageImporter.setPropertyDataImportService( propertyDataImportService );
	}

	@Test(expected = WebCmsDataImportException.class)
	public void validateObjectIdFailsIfNotMatchingObjectId() {
		WebCmsDataEntry data = new WebCmsDataEntry( null, Collections.singleton( Collections.singletonMap( "objectId", "wcm:assets:page:invalid-page-id" ) ) );
		pageImporter.importData( data );
	}

	@Test
	public void validateObjectIdWithMatchingObjectId() {
		WebCmsDataEntry data = new WebCmsDataEntry( null, Collections.singleton( Collections.singletonMap( "objectId", "wcm:asset:page:valid-page-id" ) ) );
		pageImporter.importData( data );
	}
}
