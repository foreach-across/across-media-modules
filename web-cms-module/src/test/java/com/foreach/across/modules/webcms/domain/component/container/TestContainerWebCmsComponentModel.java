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

package com.foreach.across.modules.webcms.domain.component.container;

import com.foreach.across.modules.webcms.domain.component.MyComponentMetadata;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestContainerWebCmsComponentModel
{
	private WebCmsComponentType componentType = WebCmsComponentType.builder().build();
	private ContainerWebCmsComponentModel model = new ContainerWebCmsComponentModel( componentType );

	@Test
	public void addComponentModels() {
		assertNull( model.getMetadata() );
		assertFalse( model.hasMetadata() );
		assertTrue( model.isEmpty() );

		TextWebCmsComponentModel title = new TextWebCmsComponentModel( WebCmsComponent.builder().id( 1L ).name( "title" ).build() );
		ContainerWebCmsComponentModel body = new ContainerWebCmsComponentModel( WebCmsComponent.builder().id( 2L ).name( "body" ).build() );

		model.addMember( title );
		model.addMember( body );

		assertFalse( model.isEmpty() );
		assertEquals( Arrays.asList( title, body ), model.getMembers() );

		assertSame( title, model.getMember( "title" ) );
		assertSame( body, model.getMember( "body" ) );
	}

	@Test
	public void markupIsSupportedIfComponentTypeHasCorrectAttributeSetToTrue() {
		assertFalse( model.isMarkupSupported() );

		componentType.getAttributes().put( ContainerWebCmsComponentModel.SUPPORTS_MARKUP_ATTRIBUTE, "true" );
		assertTrue( model.isMarkupSupported() );
		componentType.getAttributes().put( ContainerWebCmsComponentModel.SUPPORTS_MARKUP_ATTRIBUTE, "false" );
		assertFalse( model.isMarkupSupported() );
	}

	@Test
	public void markupIsSetIfNotEmpty() {
		assertFalse( model.hasMarkup() );
		model.setMarkup( "" );
		assertFalse( model.hasMarkup() );
		model.setMarkup( "some markup" );
		assertTrue( model.hasMarkup() );
		model.setMarkup( null );
		assertFalse( model.hasMarkup() );
	}

	@Test
	public void asTemplateShouldAlsoCreateTemplatesOfMembers() {
		WebCmsComponent component = WebCmsComponent.builder()
		                                           .id( 123L )
		                                           .name( "component-name" )
		                                           .title( "My title" )
		                                           .componentType( componentType )
		                                           .ownerObjectId( "123" )
		                                           .build();

		model = new ContainerWebCmsComponentModel( component );

		TextWebCmsComponentModel title = new TextWebCmsComponentModel( WebCmsComponent.builder().id( 1L ).name( "title" ).build() );
		assertFalse( title.isNew() );
		ContainerWebCmsComponentModel body = new ContainerWebCmsComponentModel( WebCmsComponent.builder().id( 2L ).name( "body" ).build() );
		assertFalse( body.isNew() );

		model.setMarkup( "my component markup" );
		MyComponentMetadata metadata = MyComponentMetadata.builder().title( "some title" ).number( 123L ).page( new WebCmsPage() ).build();
		model.setMetadata( metadata );
		model.addMember( title );
		model.addMember( body );

		ContainerWebCmsComponentModel template = model.asComponentTemplate();
		assertNotEquals( component, template.getComponent() );
		assertSame( componentType, template.getComponentType() );
		assertEquals( "component-name", template.getName() );
		assertEquals( "My title", template.getTitle() );
		assertNotEquals( component.getObjectId(), template.getObjectId() );
		assertNotEquals( "123", template.getOwnerObjectId() );
		assertTrue( template.isNew() );
		assertEquals( "my component markup", template.getMarkup() );

		assertTrue( template.hasMetadata() );
		MyComponentMetadata cloned = template.getMetadata( MyComponentMetadata.class );
		assertNotSame( metadata, cloned );
		assertEquals( metadata.getTitle(), cloned.getTitle() );
		assertEquals( metadata.getNumber(), cloned.getNumber() );
		assertSame( metadata.getPage(), cloned.getPage() );

		assertEquals( 2, template.getMembers().size() );
		assertTrue( template.getMember( "title" ).isNew() );
		assertTrue( template.getMember( "title" ) instanceof TextWebCmsComponentModel );
		assertTrue( template.getMember( "body" ).isNew() );
		assertTrue( template.getMember( "body" ) instanceof ContainerWebCmsComponentModel );
	}
}
