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

package it.components;

import com.foreach.across.modules.webcms.data.WebCmsDataImportService;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import it.AbstractCmsApplicationWithTestDataIT;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@DirtiesContext
public class ITComponentImportAndCreation extends AbstractCmsApplicationWithTestDataIT
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

	@Test
	public void updatingWithoutChangingComponentTypeKeepsProperties() {
		importComponent( "plain-text" );

		TextWebCmsComponentModel componentModel = componentModelService.getComponentModelByName( componentName, null, TextWebCmsComponentModel.class );
		assertNotNull( componentModel );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, componentModel.getMarkupType() );
		componentModel.setContent( "test content" );
		assertEquals( 0, componentModel.getSortIndex() );
		componentModelService.save( componentModel );

		Map<String, Object> componentDef = new LinkedHashMap<>();
		componentDef.put( "name", componentName );
		componentDef.put( "componentType", "plain-text" );
		componentDef.put( "sortIndex", 33 );

		dataImportService.importData(
				Collections.singletonMap( "assets", Collections.singletonMap( "component", Collections.singletonList( componentDef ) ) )
		);

		TextWebCmsComponentModel updated = componentModelService.getComponentModelByName( componentName, null, TextWebCmsComponentModel.class );
		assertNotNull( updated );
		assertEquals( componentModel, updated );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, updated.getMarkupType() );
		assertEquals( "test content", updated.getContent() );
		assertEquals( 33, updated.getSortIndex() );
	}

	@Test
	public void updatingContainerKeepsOnlyAddedMembers() {
		ContainerWebCmsComponentModel container = componentModelService.createComponentModel( ContainerWebCmsComponentModel.TYPE_DYNAMIC,
		                                                                                      ContainerWebCmsComponentModel.class );
		container.setName( UUID.randomUUID().toString() );

		TextWebCmsComponentModel text = componentModelService.createComponentModel( "plain-text", TextWebCmsComponentModel.class );
		text.setContent( "Some text" );
		container.addMember( text );

		componentModelService.save( container );

		ContainerWebCmsComponentModel fetched = componentModelService.getComponentModel( container.getObjectId(), ContainerWebCmsComponentModel.class );
		assertEquals( container, fetched );
		assertEquals( 1, fetched.size() );

		fetched.getMembers().clear();
		componentModelService.save( fetched );

		fetched = componentModelService.getComponentModel( container.getObjectId(), ContainerWebCmsComponentModel.class );
		assertTrue( fetched.isEmpty() );
	}

	private void importComponent( String componentType ) {
		Map<String, Object> componentDef = new LinkedHashMap<>();
		componentDef.put( "name", componentName );
		componentDef.put( "componentType", componentType );

		dataImportService.importData(
				Collections.singletonMap( "assets", Collections.singletonMap( "component", Collections.singletonList( componentDef ) ) )
		);
	}

	@Test
	public void creatingComponentOfTypeCreatesTheDifferentMembers() {
		ContainerWebCmsComponentModel container = componentModelService.createComponentModel( "teaser", ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( "Teaser title", container.getMember( "title", TextWebCmsComponentModel.class ).getContent() );
		assertTrue( container.getMember( "body", TextWebCmsComponentModel.class ).isEmpty() );
		assertEquals( "teaser", container.getComponentType().getTypeKey() );

		container.setName( componentName );
		container.getMember( "title", TextWebCmsComponentModel.class ).setContent( "modified teaser title" );
		componentModelService.save( container );

		ContainerWebCmsComponentModel fetched = componentModelService.getComponentModelByName( componentName, null, ContainerWebCmsComponentModel.class );
		assertEquals( container, fetched );
		assertEquals( "modified teaser title", container.getMember( "title", TextWebCmsComponentModel.class ).getContent() );
		assertTrue( container.getMember( "body", TextWebCmsComponentModel.class ).isEmpty() );
	}

	@Test
	public void updatedComponentOfSpecificType() {
		ContainerWebCmsComponentModel container = componentModelService.getComponentModelByName( "test-teaser", null, ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( "Teaser title", container.getMember( "title", TextWebCmsComponentModel.class ).getContent() );
		assertEquals( "Sample teaser body", container.getMember( "body", TextWebCmsComponentModel.class ).getContent() );
	}
}
