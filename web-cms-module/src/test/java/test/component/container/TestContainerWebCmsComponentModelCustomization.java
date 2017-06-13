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

package test.component.container;

import com.foreach.across.modules.webcms.domain.component.*;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import test.component.text.TestTextWebCmsComponentModelCustomization.MyMetadata;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.AbstractWebCmsComponentModelRenderingTest;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestContainerWebCmsComponentModelCustomization extends AbstractWebCmsComponentModelRenderingTest
{
	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Autowired
	private WebCmsComponentRepository componentRepository;

	@Autowired
	public void registerCustomComponentType( WebCmsComponentTypeRepository typeRepository ) {
		if ( typeRepository.findOneByTypeKey( "custom-container" ) == null ) {
			typeRepository.save(
					WebCmsComponentType.builder()
					                   .name( "Custom container component" )
					                   .typeKey( "custom-container" )
					                   .attribute( "type", "container" )
					                   .attribute( "metadata", MyMetadata.class.getName() )
					                   .attribute( "template", "th/test/fragments :: customContainer" )
					                   .build()
			);
		}
	}

	@Test
	public void newComponentShouldHaveDefaultTypedMetadata() {
		val model = componentModelService.createComponentModel( "custom-container", ContainerWebCmsComponentModel.class );
		assertNotNull( model );

		assertTrue( model.hasMetadata() );

		Object rawMetadata = model.getMetadata();
		assertNotNull( rawMetadata );
		assertTrue( rawMetadata instanceof MyMetadata );
		MyMetadata metadata = model.getMetadata( MyMetadata.class );
		assertSame( metadata, rawMetadata );

		assertEquals( MyMetadata.Country.BELGIUM, metadata.getCountry() );
		assertNull( metadata.getTitle() );
		assertTrue( metadata.isEnabled() );
	}

	@Test
	public void saveReadAndUpdateComponent() {
		val model = componentModelService.createComponentModel( "custom-container", ContainerWebCmsComponentModel.class );

		TextWebCmsComponentModel title = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		title.setName( "title" );
		title.setContent( "title text" );
		model.addMember( title );

		MyMetadata metadata = model.getMetadata( MyMetadata.class );
		metadata.setEnabled( false );
		metadata.setTitle( "My custom title" );
		metadata.setCountry( null );

		WebCmsComponent component = componentModelService.save( model );
		assertNotNull( component.getMetadata() );
		assertFalse( StringUtils.isBlank( component.getMetadata() ) );

		val fetched = componentModelService.buildModelForComponent(
				componentRepository.findOneByObjectId( component.getObjectId() ), ContainerWebCmsComponentModel.class
		);
		assertEquals( title, fetched.getMember( "title" ) );
		metadata = fetched.getMetadata( MyMetadata.class );
		assertEquals( "My custom title", metadata.getTitle() );
		assertNull( metadata.getCountry() );
		assertFalse( metadata.isEnabled() );

		metadata.setCountry( MyMetadata.Country.NETHERLANDS );
		metadata.setTitle( null );
		componentModelService.save( fetched );

		val updated = componentModelService.buildModelForComponent(
				componentRepository.findOneByObjectId( component.getObjectId() ), ContainerWebCmsComponentModel.class
		);
		assertEquals( title, updated.getMember( "title" ) );
		metadata = fetched.getMetadata( MyMetadata.class );
		assertNull( metadata.getTitle() );
		assertEquals( MyMetadata.Country.NETHERLANDS, metadata.getCountry() );
		assertFalse( metadata.isEnabled() );
	}

	@Test
	public void customComponentRendering() {
		val model = componentModelService.createComponentModel( "custom-container", ContainerWebCmsComponentModel.class );

		TextWebCmsComponentModel title = componentModelService.createComponentModel( "html", TextWebCmsComponentModel.class );
		title.setName( "title" );
		title.setContent( "title text" );
		model.addMember( title );

		renderAndExpect( model, "<div><h1>title text</h1></div>" );

		val metadata = model.getMetadata( MyMetadata.class );
		metadata.setTitle( "Fixed title." );
		renderAndExpect( model, "<div><h1>Fixed title.</h1></div>" );

		metadata.setEnabled( false );
		renderAndExpect( model, "<div><p>disabled</p><h1>Fixed title.</h1></div>" );
	}

}
