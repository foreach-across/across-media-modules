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

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestContainerWebCmsComponentModel
{
	private ContainerWebCmsComponentModel model;

	@Test
	public void addComponentModels() {
		model = new ContainerWebCmsComponentModel();
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
	public void asTemplateShouldAlsoCreateTemplatesOfMembers() {
		WebCmsComponentType componentType = new WebCmsComponentType();
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

		model.addMember( title );
		model.addMember( body );

		ContainerWebCmsComponentModel template = model.asTemplate();
		assertNotEquals( component, template.getComponent() );
		assertSame( componentType, template.getComponentType() );
		assertEquals( "component-name", template.getName() );
		assertEquals( "My title", template.getTitle() );
		assertNotEquals( component.getObjectId(), template.getObjectId() );
		assertNotEquals( "123", template.getOwnerObjectId() );
		assertTrue( template.isNew() );

		assertEquals( 2, template.getMembers().size() );
		assertTrue( template.getMember( "title" ).isNew() );
		assertTrue( template.getMember( "title" ) instanceof TextWebCmsComponentModel );
		assertTrue( template.getMember( "body" ).isNew() );
		assertTrue( template.getMember( "body" ) instanceof ContainerWebCmsComponentModel );
	}
}
