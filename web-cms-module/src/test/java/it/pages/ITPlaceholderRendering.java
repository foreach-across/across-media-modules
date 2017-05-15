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
import com.foreach.across.modules.webcms.domain.component.placeholder.PlaceholderWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractSingleApplicationIT;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class ITPlaceholderRendering extends AbstractSingleApplicationIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	private static Html html;

	@Before
	public void setUp() throws Exception {
		if ( html == null ) {
			html = html( "/render-placeholders" );
		}
	}

	@Test
	public void verifyGlobalComponentHasBeenInstalled() {
		val content = componentModelService.getComponentModelByName( "with-placeholders", null, ContainerWebCmsComponentModel.class );
		assertNotNull( content );
		assertEquals( "with-placeholders", content.getName() );
		assertEquals( "With placeholders", content.getTitle() );
		assertEquals( 3, content.size() );

		TextWebCmsComponentModel heading = content.getMember( "heading", TextWebCmsComponentModel.class );
		assertNotNull( heading );
		assertEquals( "Heading", heading.getTitle() );
		assertEquals( "heading", heading.getName() );
		assertEquals( "Global placeholders:", heading.getContent() );
		assertEquals( 0, heading.getComponent().getSortIndex() );

		PlaceholderWebCmsComponentModel two = content.getMember( "two", PlaceholderWebCmsComponentModel.class );
		assertNotNull( two );
		assertEquals( "Two", two.getTitle() );
		assertEquals( "two", two.getName() );
		assertEquals( "two", two.getPlaceholderName() );
		assertEquals( 1, two.getComponent().getSortIndex() );

		PlaceholderWebCmsComponentModel three = content.getMember( "three", PlaceholderWebCmsComponentModel.class );
		assertNotNull( three );
		assertEquals( "Three", three.getTitle() );
		assertEquals( "three", three.getName() );
		assertEquals( "three", three.getPlaceholderName() );
		assertEquals( 2, three.getComponent().getSortIndex() );
	}

	@Test
	public void verifyPageAndComponentsHaveBeenInstalled() {
		val page = pageService.findByCanonicalPath( "/render-placeholders" )
		                      .orElse( null );
		assertNotNull( page );

		assertEquals( "wcm:asset:page:render-placeholders", page.getObjectId() );
		assertEquals( "Render placeholders", page.getTitle() );
		assertEquals( "th/test/pages/render-placeholders", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "render-placeholders", page.getPathSegment() );
		assertEquals( "/render-placeholders", page.getCanonicalPath() );
		assertNull( page.getParent() );

		val content = componentModelService.getComponentModelByName( "with-placeholders", page, ContainerWebCmsComponentModel.class );
		assertNotNull( content );
		assertEquals( "with-placeholders", content.getName() );
		assertEquals( "With placeholders", content.getTitle() );
		assertEquals( 4, content.size() );

		TextWebCmsComponentModel heading = content.getMember( "heading", TextWebCmsComponentModel.class );
		assertNotNull( heading );
		assertEquals( "Heading", heading.getTitle() );
		assertEquals( "heading", heading.getName() );
		assertEquals( "Placeholders:", heading.getContent() );
		assertEquals( 0, heading.getComponent().getSortIndex() );

		PlaceholderWebCmsComponentModel one = content.getMember( "one", PlaceholderWebCmsComponentModel.class );
		assertNotNull( one );
		assertEquals( "One", one.getTitle() );
		assertEquals( "one", one.getName() );
		assertEquals( "one", one.getPlaceholderName() );
		assertEquals( 1, one.getComponent().getSortIndex() );

		PlaceholderWebCmsComponentModel two = content.getMember( "two", PlaceholderWebCmsComponentModel.class );
		assertNotNull( two );
		assertEquals( "Two", two.getTitle() );
		assertEquals( "two", two.getName() );
		assertEquals( "two", two.getPlaceholderName() );
		assertEquals( 3, two.getComponent().getSortIndex() );

		PlaceholderWebCmsComponentModel three = content.getMember( "three", PlaceholderWebCmsComponentModel.class );
		assertNotNull( three );
		assertEquals( "Three", three.getTitle() );
		assertEquals( "three", three.getName() );
		assertEquals( "three", three.getPlaceholderName() );
		assertEquals( 2, three.getComponent().getSortIndex() );
	}

	@Test
	public void placeholdersAreParsedAndRenderedIfComponent() {
		html.assertElementHasHTML( "Placeholders:<div>one</div>threetwo", "#placeholders-rendered" );
		html.assertElementHasHTML( "Global placeholders:twothree", "#placeholders-rendered-in-global-component" );
	}

	@Test
	public void placeholderAttributesAreIgnoredIfNotInComponent() {
		html.assertElementHasHTML( "<div>one</div>", "#no-component" );
	}

	@Test
	public void placeholderAttributesAreIgnoredIfComponentNotFound() {
		html.assertElementHasHTML( "First: <strong>one</strong> and second: <em>two</em>.", "#not-found-default-markup" );
	}

	@Test
	public void placeholderAttributesAreIgnoredIfNotInParsingBlock() {
		html.assertElementHasHTML( "Placeholders:", "#placeholders-not-parsed" );
	}

	@Test
	public void placeholdersInsideSingleParsingBlockShouldBeRegisteredEvenIfInsideComponentBlocks() {
		html.assertElementHasHTML( "Placeholders:<div>one</div><li>three</li>two", "#nested-placeholders" );
	}

	@Test
	public void componentsInsidePlaceholdersShouldAlwaysBeRendered() {
		html.assertElementHasHTML(
				"Placeholders:<div>Global component: footer</div>threeGlobal component: content",
				"#placeholder-that-contains-component"
		);
	}
}
