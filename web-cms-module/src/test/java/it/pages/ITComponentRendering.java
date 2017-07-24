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

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractCmsApplicationWithTestDataIT;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class ITComponentRendering extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	private static Html html;

	@Before
	public void setUp() throws Exception {
		if ( html == null ) {
			html = html( "/render-components" );
		}
	}

	@Test
	public void verifyPageAndComponentsHaveBeenInstalled() {
		val page = pageService.findByCanonicalPath( "/render-components" )
		                      .orElse( null );
		assertNotNull( page );

		assertEquals( "wcm:asset:page:render-components", page.getObjectId() );
		assertEquals( "Render components", page.getTitle() );
		assertEquals( "th/test/pages/render-components", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "render-components", page.getPathSegment() );
		assertEquals( "/render-components", page.getCanonicalPath() );
		assertNull( page.getParent() );

		val content = componentModelService.getComponentModelByName( "content", page, TextWebCmsComponentModel.class );
		assertNotNull( content );
		assertEquals( "content", content.getName() );
		assertEquals( "Content", content.getTitle() );
		assertEquals( "Page component: content 2", content.getContent() );

		val custom = componentModelService.getComponentModelByName( "custom", page, TextWebCmsComponentModel.class );
		assertNotNull( custom );
		assertEquals( "custom", custom.getName() );
		assertEquals( "Custom", custom.getTitle() );
		assertEquals( "Page component: custom", custom.getContent() );
	}

	@Test
	public void ifComponentIsNotFoundTheRegularMarkupIsRendered() {
		html.assertElementHasText( "Not found - default markup.", "#not-found-default-markup" );
	}

	@Test
	public void markupIsAlwaysReplacedEvenIfNoComponentWhenAttributeIsPresent() {
		html.assertElementIsEmpty( "#not-found-replaced" );
	}

	@Test
	public void componentFoundInDefaultScope() {
		html.assertElementHasText( "Page component: custom", "#found-in-page" );
	}

	@Test
	public void parentScopesAreSearchedIfNoScopeAndNotExplicitlyDenied() {
		html.assertElementHasText( "Global component: footer", "#found-in-global" );
	}

	@Test
	public void componentFoundInLowestScopeTakesPrecedence() {
		html.assertElementHasText( "Page component: content 2", "#shadowing-global" );
	}

	@Test
	public void specifiedScopeIsAlwaysUsed() {
		html.assertElementHasText( "Global component: content", "#using-global" );
	}

	@Test
	public void notSearchingParentScopesIfExplicitlyDenied() {
		html.assertElementHasText( "Not found in scope and not searching parents.", "#not-found-in-scope" );
	}

	@Test
	public void notSearchParentScopesIfScopeSpecifiedAndNotExplicitlyEnabled() {
		html.assertElementHasText( "Not found in scope specified and not searching parents.", "#not-found-in-scope-specified" );
	}

	@Test
	public void searchingParentScopesIfScopeSpecifiedButExplicitlyEnabled() {
		html.assertElementHasText( "Global component: footer", "#found-by-search" );
	}

	@Test
	public void selfClosingTagIsSupported() {
		html.assertElementHasText( "Page component: custom", "#self-closing-tag" );
	}
}
