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
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractSingleApplicationIT;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ITAutoCreateComplexContainerComponents extends AbstractSingleApplicationIT
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
			page = pageService.findByCanonicalPath( "/auto-create-complex-components" )
			                  .orElse( null );
			verifyPageDoesButNoneOfTheComponentsExist( page );
			html = html( "/auto-create-complex-components" );
		}
	}

	public void verifyPageDoesButNoneOfTheComponentsExist( WebCmsPage page ) {
		assertNotNull( page );

		assertEquals( "wcm:asset:page:auto-create-complex-components", page.getObjectId() );
		assertEquals( "Auto create complex components", page.getTitle() );
		assertEquals( "th/test/pages/auto-create-complex-components", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "auto-create-complex-components", page.getPathSegment() );
		assertEquals( "/auto-create-complex-components", page.getCanonicalPath() );
		assertNull( page.getParent() );

		assertFalse( componentModelService.getComponentModelsForOwner( page ).hasOrderedComponents() );
	}

	@Test
	public void placeholderInsideContainerResultsInPlaceholderMember() {
		html.assertElementHasHTML( "<div>single placeholder content</div>", "#container-single-placeholder" );

		val container = componentModelService.getComponentModelByName( "container-single-placeholder", page, ContainerWebCmsComponentModel.class );
		assertEquals( 1, container.size() );

		val placeholder = container.getMember( "one", PlaceholderWebCmsComponentModel.class );
		assertNotNull( placeholder );
		assertEquals( "One", placeholder.getTitle() );
		assertEquals( "one", placeholder.getPlaceholderName() );
	}

	@Test
	public void nestedPlaceholdersAllResultInMembersOnTheRightLevel() {
		html.assertElementHasHTML( "container titlecontainer sub titlebefore text placeholder<div>footer text placeholder</div>", "#nested-placeholders" );

		val container = componentModelService.getComponentModelByName( "container-nested-placeholders", page, ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( 3, container.size() );

		val title = container.getMember( "title", PlaceholderWebCmsComponentModel.class );
		assertNotNull( title );
		assertEquals( "title", title.getPlaceholderName() );

		val body = container.getMember( "body", ContainerWebCmsComponentModel.class );
		assertNotNull( body );
		assertEquals( 3, body.size() );
		val subTitle = body.getMember( "subtitle", TextWebCmsComponentModel.class );
		assertNotNull( subTitle );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, subTitle.getMarkupType() );
		assertEquals( "container sub title", subTitle.getContent() );
		val beforeText = body.getMember( "before-text", PlaceholderWebCmsComponentModel.class );
		assertNotNull( beforeText );
		assertEquals( "before-text", beforeText.getPlaceholderName() );
		val textContainer = body.getMember( "text", ContainerWebCmsComponentModel.class );
		assertNotNull( textContainer );
		assertTrue( textContainer.isEmpty() );

		val globalFooter = componentModelService.getComponentModelByName( "footer", null );
		assertNotNull( globalFooter );
		val footer = container.getMember( "footer", ContainerWebCmsComponentModel.class );
		assertNotSame( footer, globalFooter );
		assertEquals( 1, footer.size() );
		assertNotNull( body );
		val footerText = footer.getMember( "footer-text", PlaceholderWebCmsComponentModel.class );
		assertNotNull( footerText );
		assertEquals( "footer-text", footerText.getPlaceholderName() );
	}

	@Test
	public void componentsInsidePlaceholdersInsideContainerShouldNotBeContainerMembers() {
		html.assertElementHasHTML( "<div> Placeholder component 1: new component </div>Placeholder component 2: <div>Global component: footer</div>",
		                           "#component-in-placeholders" );

		val container = componentModelService.getComponentModelByName( "component-in-placeholders", page, ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( 2, container.size() );

		val header = container.getMember( "header", PlaceholderWebCmsComponentModel.class );
		assertNotNull( header );
		assertEquals( "header", header.getPlaceholderName() );

		val body = container.getMember( "body", ContainerWebCmsComponentModel.class );
		assertNotNull( body );
		assertEquals( 1, body.size() );
		val bodyText = body.getMember( "body-text", PlaceholderWebCmsComponentModel.class );
		assertNotNull( bodyText );
		assertEquals( "body-text", bodyText.getPlaceholderName() );

		assertNull( componentModelService.getComponentModelByName( "not-created", page ) );
		val headerText = componentModelService.getComponentModelByName( "header-text", page, TextWebCmsComponentModel.class );
		assertNotNull( headerText );
		assertEquals( "new component", headerText.getContent() );
	}

	@Test
	public void placeholdersInsideMarkupResultInPlaceholderMarkers() {
		html.assertElementHasHTML( "Default markup <span>one</span> with two content", "#markup-with-placeholders" );

		val markup = componentModelService.getComponentModelByName( "markup-with-placeholders", page, TextWebCmsComponentModel.class );
		assertEquals( "Default markup @@wcm:placeholder(one)@@ with @@wcm:placeholder(two)@@ content", markup.getContent() );
	}

	@Test
	public void secondRenderYieldsSameOutput() {
		Html secondRender = html( "/auto-create-complex-components" );
		secondRender.assertElementHasHTML( "<div>single placeholder content</div>", "#container-single-placeholder" );
		secondRender.assertElementHasHTML( "container titlecontainer sub titlebefore text placeholder<div>footer text placeholder</div>",
		                                   "#nested-placeholders" );
		secondRender.assertElementHasHTML( "<div> Placeholder component 1: new component </div>Placeholder component 2: <div>Global component: footer</div>",
		                                   "#component-in-placeholders" );
		secondRender.assertElementHasHTML( "Default markup <span>one</span> with two content", "#markup-with-placeholders" );
	}

	// container with content
	// container but members that have explicit different scope should result in proxies
	// container with member that has explicit scope specified (should not auto-create member but create a proxy)
}
