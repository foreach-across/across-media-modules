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
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.placeholder.PlaceholderWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.proxy.ProxyWebCmsComponentModel;
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
			page = pageService.findByCanonicalPath( "/auto-create-complex-container-components" )
			                  .orElse( null );
			verifyPageDoesButNoneOfTheComponentsExist( page );
			html = html( "/auto-create-complex-container-components" );
		}
	}

	public void verifyPageDoesButNoneOfTheComponentsExist( WebCmsPage page ) {
		assertNotNull( page );

		assertEquals( "wcm:asset:page:auto-create-complex-container-components", page.getObjectId() );
		assertEquals( "Auto create complex container components", page.getTitle() );
		assertEquals( "th/test/pages/auto-create/complex-container-components", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "auto-create-complex-container-components", page.getPathSegment() );
		assertEquals( "/auto-create-complex-container-components", page.getCanonicalPath() );
		assertNull( page.getParent() );

		assertTrue( componentModelService.getComponentModelsForOwner( page ).isEmpty() );
	}

	@Test
	public void placeholderInsideContainerResultsInPlaceholderMember() {
		val container = componentModelService.getComponentModelByName( "container-single-placeholder", page, ContainerWebCmsComponentModel.class );
		assertEquals( 1, container.size() );

		val placeholder = container.getMember( "one", PlaceholderWebCmsComponentModel.class );
		assertNotNull( placeholder );
		assertEquals( "One", placeholder.getTitle() );
		assertEquals( "one", placeholder.getPlaceholderName() );

		assertEqualsIgnoreWhitespace( "@@wcm:component(one,container,false)@@", container.getMarkup() );

		html.assertElementHasHTML( "<div>single placeholder content</div>", "#container-single-placeholder" );
	}

	@Test
	public void nestedPlaceholdersAllResultInMembersOnTheRightLevel() {
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

		assertEqualsIgnoreWhitespace(
				"@@wcm:component(subtitle,container,false)@@ @@wcm:component(before-text,container,false)@@ @@wcm:component(text,container,false)@@",
				body.getMarkup()
		);

		val globalFooter = componentModelService.getComponentModelByName( "footer", null );
		assertNotNull( globalFooter );
		val footer = container.getMember( "footer", ContainerWebCmsComponentModel.class );
		assertNotSame( footer, globalFooter );
		assertEquals( 1, footer.size() );
		assertNotNull( body );
		val footerText = footer.getMember( "footer-text", PlaceholderWebCmsComponentModel.class );
		assertNotNull( footerText );
		assertEquals( "footer-text", footerText.getPlaceholderName() );

		assertEqualsIgnoreWhitespace(
				"@@wcm:component(title,container,false)@@ @@wcm:component(body,container,false)@@ <div>@@wcm:component(footer,container,false)@@</div>",
				container.getMarkup()
		);

		html.assertElementHasHTML( "container titlecontainer sub titlebefore text placeholder<div>footer text placeholder</div>", "#nested-placeholders" );
	}

	@Test
	public void componentsInsidePlaceholdersInsideContainerShouldNotBeContainerMembers() {
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
		assertEqualsIgnoreWhitespace( "@@wcm:component(body-text,container,false)@@", body.getMarkup() );

		assertNull( componentModelService.getComponentModelByName( "not-created", page ) );
		val headerText = componentModelService.getComponentModelByName( "header-text", page, TextWebCmsComponentModel.class );
		assertNotNull( headerText );
		assertEquals( "new component", headerText.getContent() );

		assertEqualsIgnoreWhitespace( "@@wcm:component(header,container,false)@@ @@wcm:component(body,container,false)@@", container.getMarkup() );

		html.assertElementHasHTML( "<div> Placeholder component 1: new component </div>Placeholder component 2: <div>Global component: footer</div>",
		                           "#component-in-placeholders" );
	}

	@Test
	public void scopedComponentResultsInProxyComponent() {
		val container = componentModelService.getComponentModelByName( "proxy-component-in-container", page, ContainerWebCmsComponentModel.class );
		assertEquals( 1, container.size() );

		val proxy = container.getMember( "footer", ProxyWebCmsComponentModel.class );
		assertNotNull( proxy );
		assertEquals( "footer", proxy.getName() );
		assertEquals( "Footer", proxy.getTitle() );
		WebCmsComponentModel footer = componentModelService.getComponentModelByName( "footer", null );
		assertEquals( footer, proxy.getTarget() );

		assertEqualsIgnoreWhitespace( "Insert footer: <div>@@wcm:component(footer,container,false)@@</div>", container.getMarkup() );

		html.assertElementHasHTML( "Global component: footer", "#proxy-component-in-container" );
	}

	@Test
	public void autoCreationOfScopedComponentsAlsoResultsInProxyComponent() {
		val footer = componentModelService.getComponentModelByName( "auto-create-proxy-footer", page, TextWebCmsComponentModel.class );
		assertNotNull( footer );
		assertEqualsIgnoreWhitespace( "auto-created on asset", footer.getContent() );

		val global = componentModelService.getComponentModelByName( "auto-create-proxy-global", null, TextWebCmsComponentModel.class );
		assertNotNull( global );
		assertEqualsIgnoreWhitespace( "auto-created on global", global.getContent() );

		val container = componentModelService.getComponentModelByName( "auto-create-proxy-component-in-container", page, ContainerWebCmsComponentModel.class );
		assertEquals( 2, container.size() );

		val footerProxy = container.getMember( "auto-create-proxy-footer", ProxyWebCmsComponentModel.class );
		assertNotNull( footerProxy );
		assertEquals( footer, footerProxy.getTarget() );

		val globalProxy = container.getMember( "auto-create-proxy-global", ProxyWebCmsComponentModel.class );
		assertNotNull( globalProxy );
		assertEquals( global, globalProxy.getTarget() );

		assertEqualsIgnoreWhitespace( "Create footer <div>@@wcm:component(auto-create-proxy-footer,container,false)@@</div> " +
				                              "and on global <div>@@wcm:component(auto-create-proxy-global,container,false)@@</div>", container.getMarkup() );

		html.assertElementHasHTML( "auto-created on assetauto-created on global", "#auto-create-proxy-component-in-container" );
	}

	@Test
	public void secondRenderYieldsSameOutput() {
		Html secondRender = html( "/auto-create-complex-container-components" );
		secondRender.assertElementHasHTML( "<div>single placeholder content</div>", "#container-single-placeholder" );
		secondRender.assertElementHasHTML( "container titlecontainer sub titlebefore text placeholder<div>footer text placeholder</div>",
		                                   "#nested-placeholders" );
		secondRender.assertElementHasHTML( "<div> Placeholder component 1: new component </div>Placeholder component 2: <div>Global component: footer</div>",
		                                   "#component-in-placeholders" );
		secondRender.assertElementHasHTML( "Global component: footer", "#proxy-component-in-container" );
		secondRender.assertElementHasHTML( "auto-created on assetauto-created on global", "#auto-create-proxy-component-in-container" );
	}
}
