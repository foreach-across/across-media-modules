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

package com.foreach.across.modules.webcms.domain.component.text;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestTextWebCmsComponentModel
{
	private TextWebCmsComponentModel model;

	@Test
	public void defaultValues() {
		model = new TextWebCmsComponentModel();
		assertNull(  model.getMetadata() );
		assertFalse( model.hasMetadata() );
		assertNull( model.getComponentType() );
		assertNull( model.getName() );
		assertNotNull( model.getObjectId() );
		assertNull( model.getOwnerObjectId() );
		assertNotNull( model.getComponent() );
		assertEquals( WebCmsComponentModel.class.getSimpleName(), model.getElementType() );
		assertNull( model.getCustomTemplate() );
		assertTrue( model.isNew() );

		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, model.getMarkupType() );
		assertNull( model.getProfile() );
		assertTrue( model.isMultiLine() );
		assertNull( model.getContent() );
		assertTrue( model.isEmpty() );
	}

	@Test
	public void fromComponent() {
		WebCmsComponentType componentType = WebCmsComponentType.builder().attribute( WebCmsComponentModel.TEMPLATE_ATTRIBUTE, "th/mytemplate" ).build();

		WebCmsComponent component = WebCmsComponent.builder()
		                                           .id( 123L )
		                                           .name( "component-name" )
		                                           .title( "My title" )
		                                           .componentType( componentType )
		                                           .ownerObjectId( "123" )
		                                           .build();
		model = new TextWebCmsComponentModel( component );

		assertEquals( component, model.getComponent() );
		assertSame( componentType, model.getComponentType() );
		assertEquals( "component-name", model.getName() );
		assertEquals( "My title", model.getTitle() );
		assertEquals( component.getObjectId(), model.getObjectId() );
		assertEquals( "123", model.getOwnerObjectId() );
		assertFalse( model.isNew() );

		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, model.getMarkupType() );
		assertNull( model.getProfile() );
		assertTrue( model.isMultiLine() );
		assertNull( model.getContent() );
		assertTrue( model.isEmpty() );

		assertEquals( "th/mytemplate", model.getCustomTemplate() );
		model.setRenderTemplate( "specific-template" );
		assertEquals( "specific-template", model.getCustomTemplate() );
	}

	@Test
	public void backingComponentShouldBeDto() {
		WebCmsComponent component = WebCmsComponent.builder().id( 1L ).build();

		model = new TextWebCmsComponentModel( component );
		assertEquals( component, model.getComponent() );
		assertNotSame( component, model.getComponent() );

		WebCmsComponent other = WebCmsComponent.builder().id( 2L ).build();
		model.setComponent( other );
		assertNotEquals( component, model.getComponent() );
		assertEquals( other, model.getComponent() );
		assertNotSame( other, model.getComponent() );
	}

	@Test
	public void titleReturnsNameIfNotSetExplicitly() {
		WebCmsComponent component = WebCmsComponent.builder()
		                                           .name( "component-name" )
		                                           .build();
		model = new TextWebCmsComponentModel( component );
		assertEquals( "component-name", model.getTitle() );
	}

	@Test
	public void asTemplate() {
		WebCmsComponentType componentType = new WebCmsComponentType();

		WebCmsComponent component = WebCmsComponent.builder()
		                                           .id( 123L )
		                                           .name( "component-name" )
		                                           .title( "My title" )
		                                           .componentType( componentType )
		                                           .ownerObjectId( "123" )
		                                           .build();
		model = new TextWebCmsComponentModel( component );
		model.setMultiLine( false );
		model.setMarkupType( TextWebCmsComponentModel.MarkupType.RICH_TEXT );
		model.setProfile( "some-profile" );
		model.setContent( "body content..." );

		TextWebCmsComponentModel template = model.asComponentTemplate();
		assertNotEquals( component, template.getComponent() );
		assertSame( componentType, template.getComponentType() );
		assertEquals( "component-name", template.getName() );
		assertEquals( "My title", template.getTitle() );
		assertNotEquals( component.getObjectId(), template.getObjectId() );
		assertNotEquals( "123", template.getOwnerObjectId() );
		assertTrue( template.isNew() );

		assertEquals( TextWebCmsComponentModel.MarkupType.RICH_TEXT, template.getMarkupType() );
		assertEquals( "some-profile", template.getProfile() );
		assertFalse( template.isMultiLine() );
		assertEquals( "body content...", template.getContent() );
		assertFalse( template.isEmpty() );
	}
}
