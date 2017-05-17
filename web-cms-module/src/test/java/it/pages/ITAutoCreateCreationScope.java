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
 * @since 0.0.1
 */
public class ITAutoCreateCreationScope extends AbstractSingleApplicationIT
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
			page = pageService.findByCanonicalPath( "/auto-create-creation-scope" )
			                  .orElse( null );
			verifyPageDoesButNoneOfTheComponentsExist( page );
			html = html( "/auto-create-creation-scope" );
		}
	}

	public void verifyPageDoesButNoneOfTheComponentsExist( WebCmsPage page ) {
		assertNotNull( page );

		assertEquals( "wcm:asset:page:auto-create-creation-scope", page.getObjectId() );
		assertEquals( "Auto create creation scope", page.getTitle() );
		assertEquals( "th/test/pages/auto-create/creation-scope", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "auto-create-creation-scope", page.getPathSegment() );
		assertEquals( "/auto-create-creation-scope", page.getCanonicalPath() );
		assertNull( page.getParent() );

		assertFalse( componentModelService.getComponentModelsForOwner( page ).hasOrderedComponents() );
	}

	@Test
	public void componentIsCreatedOnLowestLevelAndAsMarkupTypeByDefault() {
		html.assertElementHasText( "Page markup: Auto create creation scope", "#no-scope-or-type-specified" );

		val text = componentModelService.getComponentModelByName( "page-markup", page, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "page-markup", text.getName() );
		assertEquals( "Page markup", text.getTitle() );
		assertEquals( "Page markup: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	public void creationScopeIsTakenIntoAccount() {
		html.assertElementHasText( "Global markup: Auto create creation scope", "#creation-scope-specified" );

		assertNull( componentModelService.getComponentModelByName( "global-markup", page ) );

		val text = componentModelService.getComponentModelByName( "global-markup", null, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "global-markup", text.getName() );
		assertEquals( "Global markup", text.getTitle() );
		assertEquals( "Global markup: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	public void componentScopeIsUsedAsCreationScope() {
		html.assertElementHasText( "Global markup2: Auto create creation scope", "#component-scope-specified" );

		assertNull( componentModelService.getComponentModelByName( "global-markup2", page ) );

		val text = componentModelService.getComponentModelByName( "global-markup2", null, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "global-markup2", text.getName() );
		assertEquals( "Global markup2", text.getTitle() );
		assertEquals( "Global markup2: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	public void componentIsCreatedWithTypeSpecified() {
		html.assertElementHasText( "Page rich-text: Auto create creation scope", "#type-specified" );

		val text = componentModelService.getComponentModelByName( "page-rich-text", page, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "page-rich-text", text.getName() );
		assertEquals( "Page rich text", text.getTitle() );
		assertEquals( "Page rich-text: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.RICH_TEXT, text.getMarkupType() );
	}

	@Test
	public void defaultTypeIsContainerIfChildComponentsGetCreated() {
		html.assertElementHasHTML( "container titlecontainer body", "#default-container-type" );

		val container = componentModelService.getComponentModelByName( "page-container", page, ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( "page-container", container.getName() );
		assertEquals( "Page container", container.getTitle() );

		assertEquals( 2, container.size() );

		val title = container.getMember( "title", TextWebCmsComponentModel.class );
		assertNotNull( title );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, title.getMarkupType() );
		assertEquals( "container title", title.getContent() );

		val body = container.getMember( "body", TextWebCmsComponentModel.class );
		assertNotNull( body );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, body.getMarkupType() );
		assertEquals( "Body", body.getTitle() );
		assertEquals( "container body", body.getContent() );
	}

	@Test
	public void nestedStructureIsReflectedInContainers() {
		html.assertElementHasHTML( "container titlecontainer sub titlecontainer footer text", "#nested-containers" );

		val container = componentModelService.getComponentModelByName( "page-nested-container", page, ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( "page-nested-container", container.getName() );
		assertEquals( "Page nested container", container.getTitle() );

		assertEquals( 3, container.size() );

		val title = container.getMember( "title", TextWebCmsComponentModel.class );
		assertNotNull( title );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, title.getMarkupType() );
		assertEquals( "container title", title.getContent() );

		val body = container.getMember( "body", ContainerWebCmsComponentModel.class );
		assertEquals( 2, body.size() );
		assertNotNull( body );
		val subTitle = body.getMember( "subtitle", TextWebCmsComponentModel.class );
		assertNotNull( subTitle );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, subTitle.getMarkupType() );
		assertEquals( "container sub title", subTitle.getContent() );
		val textContainer = body.getMember( "text", ContainerWebCmsComponentModel.class );
		assertNotNull( textContainer );
		assertTrue( textContainer.isEmpty() );

		val globalFooter = componentModelService.getComponentModelByName( "footer", null );
		assertNotNull( globalFooter );
		val footer = container.getMember( "footer", ContainerWebCmsComponentModel.class );
		assertNotSame( footer, globalFooter );
		assertEquals( 1, footer.size() );
		assertNotNull( body );
		val footerText = footer.getMember( "footer-text", TextWebCmsComponentModel.class );
		assertNotNull( footerText );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, footerText.getMarkupType() );
		assertEquals( "container footer text", footerText.getContent() );
	}

	@Test
	public void secondRenderYieldsSameOutput() {
		Html secondRender = html( "/auto-create-creation-scope" );
		secondRender.assertElementHasText( "Page markup: Auto create creation scope", "#no-scope-or-type-specified" );
		secondRender.assertElementHasText( "Global markup: Auto create creation scope", "#creation-scope-specified" );
		secondRender.assertElementHasText( "Global markup2: Auto create creation scope", "#component-scope-specified" );
		secondRender.assertElementHasText( "Page rich-text: Auto create creation scope", "#type-specified" );
		secondRender.assertElementHasHTML( "container titlecontainer body", "#default-container-type" );
		secondRender.assertElementHasHTML( "container titlecontainer sub titlecontainer footer text", "#nested-containers" );
	}

	// metadata can be set (through json?)
}
