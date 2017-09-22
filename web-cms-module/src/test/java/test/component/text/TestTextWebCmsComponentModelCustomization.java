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

package test.component.text;

import com.foreach.across.modules.webcms.domain.component.*;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class TestTextWebCmsComponentModelCustomization extends AbstractWebCmsComponentModelRenderingTest
{
	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Autowired
	private WebCmsComponentRepository componentRepository;

	@Autowired
	public void registerCustomComponentType( WebCmsComponentTypeRepository typeRepository ) {
		if ( typeRepository.findOneByTypeKeyAndDomain( "custom-text", WebCmsDomain.NONE ) == null ) {
			typeRepository.save(
					WebCmsComponentType.builder()
					                   .name( "Custom text component" )
					                   .typeKey( "custom-text" )
					                   .attribute( "type", "plain-text" )
					                   .attribute( "metadata", MyMetadata.class.getName() )
					                   .attribute( "template", "th/test/fragments :: customText" )
					                   .build()
			);
		}
	}

	@Test
	public void newComponentShouldHaveDefaultTypedMetadata() {
		val model = componentModelService.createComponentModel( "custom-text", TextWebCmsComponentModel.class );
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
		val model = componentModelService.createComponentModel( "custom-text", TextWebCmsComponentModel.class );
		model.setContent( "my text..." );

		MyMetadata metadata = model.getMetadata( MyMetadata.class );
		metadata.setEnabled( false );
		metadata.setTitle( "My custom title" );
		metadata.setCountry( null );

		WebCmsComponent component = componentModelService.save( model );
		assertNotNull( component.getMetadata() );
		assertFalse( StringUtils.isBlank( component.getMetadata() ) );

		val fetched = componentModelService.buildModelForComponent(
				componentRepository.findOneByObjectId( component.getObjectId() ), TextWebCmsComponentModel.class
		);
		assertEquals( "my text...", fetched.getContent() );
		metadata = fetched.getMetadata( MyMetadata.class );
		assertEquals( "My custom title", metadata.getTitle() );
		assertNull( metadata.getCountry() );
		assertFalse( metadata.isEnabled() );

		metadata.setCountry( MyMetadata.Country.NETHERLANDS );
		metadata.setTitle( null );
		componentModelService.save( fetched );

		val updated = componentModelService.buildModelForComponent(
				componentRepository.findOneByObjectId( component.getObjectId() ), TextWebCmsComponentModel.class
		);
		assertEquals( "my text...", updated.getContent() );
		metadata = fetched.getMetadata( MyMetadata.class );
		assertNull( metadata.getTitle() );
		assertEquals( MyMetadata.Country.NETHERLANDS, metadata.getCountry() );
		assertFalse( metadata.isEnabled() );
	}

	@Test
	public void customComponentRendering() {
		val model = componentModelService.createComponentModel( "custom-text", TextWebCmsComponentModel.class );
		model.setContent( "this is the content" );

		renderAndExpect( model, "<div><h1>BELGIUM</h1><p>this is the content</p></div>" );

		val metadata = model.getMetadata( MyMetadata.class );
		metadata.setCountry( MyMetadata.Country.NETHERLANDS );
		model.setContent( "more" );
		renderAndExpect( model, "<div><h1>NETHERLANDS</h1><p>more</p></div>" );

		metadata.setEnabled( false );
		renderAndExpect( model, "<div><p>more</p></div>"  );
	}

	@Data
	@NoArgsConstructor
	public static class MyMetadata
	{
		public enum Country
		{
			BELGIUM,
			NETHERLANDS
		}

		private Country country = Country.BELGIUM;
		private String title;
		private boolean enabled = true;
	}
}
