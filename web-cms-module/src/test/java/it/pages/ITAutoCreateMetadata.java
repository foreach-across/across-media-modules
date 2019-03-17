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

package it.pages;

import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractCmsApplicationWithTestDataIT;
import lombok.val;
import modules.test.metadata.ImageWithAltMetadata;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.7
 */
public class ITAutoCreateMetadata extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	private static Html html;
	private static WebCmsPage page;

	@Before
	public void setUp() throws Exception {
		if ( html == null ) {
			page = pageService.findByCanonicalPath( "/auto-create-metadata" )
			                  .orElse( null );
			verifyPageDoesButNoneOfTheComponentsExist( page );
			html = html( "/auto-create-metadata" );
		}
	}

	public void verifyPageDoesButNoneOfTheComponentsExist( WebCmsPage page ) {
		assertNotNull( page );

		assertEquals( "wcm:asset:page:auto-create-metadata", page.getObjectId() );
		assertEquals( "Auto create metadata", page.getTitle() );
		assertEquals( "th/test/pages/auto-create/metadata-components", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "auto-create-metadata", page.getPathSegment() );
		assertEquals( "/auto-create-metadata", page.getCanonicalPath() );
		assertNull( page.getParent() );

		assertTrue( componentModelService.getComponentModelsForOwner( page, null ).isEmpty() );
	}

	@Test
	public void componentWithMetadataIsCreated() {
		html.assertElementHasText( "", "#simple-metadata" );

		val text = componentModelService.getComponentModelByName( "simple-metadata", page, ContainerWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "simple-metadata", text.getName() );
		assertEquals( "Simple metadata example", text.getTitle() );
		assertEquals( 10, text.getSortIndex() );

		ImageWithAltMetadata metadata = text.getMetadata( ImageWithAltMetadata.class );
		assertNotNull( metadata );
		assertEquals( "hello", metadata.getAltText() );
		WebCmsImage image = metadata.getImage();
		assertNotNull( image );
		assertEquals( "deer.jpg", image.getName() );
	}

	@Test
	public void componentWithAttributesIsCreated() {
		html.assertElementHasText( "", "#attributes" );

		val text = componentModelService.getComponentModelByName( "attributes", page, ContainerWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "attributes", text.getName() );
		assertEquals( "Attributes example", text.getTitle() );
		assertEquals( 10, text.getSortIndex() );

		ImageWithAltMetadata metadata = text.getMetadata( ImageWithAltMetadata.class );
		assertNotNull( metadata );
		assertEquals( "hello", metadata.getAltText() );
		WebCmsImage image = metadata.getImage();
		assertNotNull( image );
		assertEquals( "deer.jpg", image.getName() );
	}

	@Test
	public void expressionValuesAreProcessed() {
		html.assertElementHasText( "", "#expression-values" );

		val text = componentModelService.getComponentModelByName( "expression-values", page, ContainerWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "expression-values", text.getName() );
		assertEquals( "my.title", text.getTitle() );
		assertEquals( 9, text.getSortIndex() );

		ImageWithAltMetadata metadata = text.getMetadata( ImageWithAltMetadata.class );
		assertNotNull( metadata );
		assertEquals( "/hello", metadata.getAltText() );
		assertNull( metadata.getImage() );
	}

	@Test
	public void attributesAreAppliedAfterBody() {
		html.assertElementHasText( "123456", "#properties-after-body" );

		val text = componentModelService.getComponentModelByName( "properties-after-body", page, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "properties-after-body", text.getName() );
		assertEquals( "number 123", text.getTitle() );
		assertEquals( "123456", text.getContent() );
	}

	@Test
	public void componentElementAllowsShortHandAttributes() {
		html.assertElementHasText( "This is the text...", "#component-element" );

		val image1 = componentModelService.getComponentModelByName( "image-element", page, ContainerWebCmsComponentModel.class );
		assertNotNull( image1 );
		assertEquals( "image-element", image1.getName() );
		assertEquals( "Via element", image1.getTitle() );
		assertEquals( 5, image1.getSortIndex() );

		ImageWithAltMetadata metadata = image1.getMetadata( ImageWithAltMetadata.class );
		assertNotNull( metadata );
		assertEquals( "image alt text", metadata.getAltText() );

		val image2 = componentModelService.getComponentModelByName( "image-element2", page, ContainerWebCmsComponentModel.class );
		assertNotNull( image2 );
		assertEquals( "image-element2", image2.getName() );
		assertEquals( "Via element 2", image2.getTitle() );
		assertEquals( 3, image2.getSortIndex() );

		metadata = image2.getMetadata( ImageWithAltMetadata.class );
		assertNotNull( metadata );
		assertEquals( "image \"alt\" text 2", metadata.getAltText() );

		val text = componentModelService.getComponentModelByName( "text-element", page, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "text-element", text.getName() );
		assertEquals( "Text element", text.getTitle() );
		assertEquals( "This is the text...", text.getContent() );
	}

	@Test
	public void attributesCanBeSpecifiedAsElements() {
		html.assertElementHasText( "", "#attribute-elements" );

		val text = componentModelService.getComponentModelByName( "attribute-elements", page, ContainerWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "attribute-elements", text.getName() );
		assertEquals( 12, text.getSortIndex() );
		assertEquals( "Updated component title", text.getTitle() );

		ImageWithAltMetadata metadata = text.getMetadata( ImageWithAltMetadata.class );
		assertNotNull( metadata );
		assertEquals( "<strong>11</strong> numbers", metadata.getAltText() );
	}

	@Test
	public void thymeleafFragmentCanBeInsertedInMetadata() {
		html.assertElementHasText( "The number is 456.", "#fragment-via-element" );

		val text = componentModelService.getComponentModelByName( "fragment-via-element", page, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "fragment-via-element", text.getName() );
		assertEquals( "A fixed number", text.getTitle() );
		assertEquals( "The number is 456.", StringUtils.trim( text.getContent() ) );
	}
}
