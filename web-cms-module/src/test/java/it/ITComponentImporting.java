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

package it;

import com.foreach.across.modules.webcms.data.WebCmsDataImportService;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ITComponentImporting extends AbstractCmsApplicationIT
{
	private final String componentName = UUID.randomUUID().toString();

	@Autowired
	private WebCmsDataImportService dataImportService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Test
	public void changingComponentTypeResetsTheProperties() {
		importComponent( "plain-text" );

		TextWebCmsComponentModel componentModel = componentModelService.getComponentModelByName( componentName, null, TextWebCmsComponentModel.class );
		assertNotNull( componentModel );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, componentModel.getMarkupType() );
		componentModel.setContent( "test content" );
		componentModelService.save( componentModel );

		importComponent( "rich-text" );
		TextWebCmsComponentModel richText = componentModelService.getComponentModelByName( componentName, null, TextWebCmsComponentModel.class );
		assertEquals( componentModel, richText );
		assertEquals( TextWebCmsComponentModel.MarkupType.RICH_TEXT, richText.getMarkupType() );
		assertTrue( richText.isEmpty() );

		importComponent( "container" );
		ContainerWebCmsComponentModel container = componentModelService.getComponentModelByName( componentName, null, ContainerWebCmsComponentModel.class );
		assertEquals( componentModel.getComponent(), container.getComponent() );
	}

	private void importComponent( String componentType ) {
		Map<String, Object> componentDef = new LinkedHashMap<>();
		componentDef.put( "name", componentName );
		componentDef.put( "componentType", componentType );

		dataImportService.importData(
				Collections.singletonMap( "assets", Collections.singletonMap( "component", Collections.singletonList( componentDef ) ) )
		);
	}

}
