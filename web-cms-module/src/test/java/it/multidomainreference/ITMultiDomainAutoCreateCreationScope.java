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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
class ITMultiDomainAutoCreateCreationScope extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	private static Html html;
	private static WebCmsPage page;
	private static WebCmsDomain domain;

	@BeforeEach
	void setUp() {
		if ( html == null ) {
			domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
			assertNotNull( domain );

			page = pageService.findByCanonicalPathAndDomain( "/auto-create-creation-scope", domain )
			                  .orElse( null );
			verifyPageDoesButNoneOfTheComponentsExist( page );
			html = html( "http://foreach.be/auto-create-creation-scope" );
		}
	}

	void verifyPageDoesButNoneOfTheComponentsExist( WebCmsPage page ) {
		assertNotNull( page );
		assertEquals( domain, page.getDomain() );

		assertEquals( "wcm:asset:page:auto-create-creation-scope", page.getObjectId() );
		assertEquals( "Auto create creation scope", page.getTitle() );
		assertEquals( "th/test/pages/auto-create/creation-scope", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "auto-create-creation-scope", page.getPathSegment() );
		assertEquals( "/auto-create-creation-scope", page.getCanonicalPath() );
		assertNull( page.getParent() );

		assertTrue( componentModelService.getComponentModelsForOwner( page, null ).isEmpty() );
	}

	@Test
	void componentIsCreatedOnLowestLevelAndAsMarkupTypeByDefault() {
		html.assertElementHasText( "Page markup: Auto create creation scope", "#no-scope-or-type-specified" );

		val text = componentModelService.getComponentModelByNameAndDomain( "page-markup", page, domain, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( domain, text.getDomain() );
		assertEquals( "page-markup", text.getName() );
		assertEquals( "Page markup", text.getTitle() );
		assertEquals( "Page markup: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	void creationScopeIsTakenIntoAccount() {
		html.assertElementHasText( "Global markup: Auto create creation scope", "#creation-scope-specified" );

		assertNull( componentModelService.getComponentModelByName( "global-markup", page ) );

		val text = componentModelService.getComponentModelByNameAndDomain( "global-markup", null, WebCmsDomain.NONE, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertNull( text.getDomain() );
		assertEquals( "global-markup", text.getName() );
		assertEquals( "Global markup", text.getTitle() );
		assertEquals( "Global markup: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	void componentScopeIsUsedAsCreationScope() {
		html.assertElementHasText( "Global markup2: Auto create creation scope", "#component-scope-specified" );

		assertNull( componentModelService.getComponentModelByName( "global-markup2", page ) );

		val text = componentModelService.getComponentModelByNameAndDomain( "global-markup2", null, WebCmsDomain.NONE, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertNull( text.getDomain() );
		assertEquals( "global-markup2", text.getName() );
		assertEquals( "Global markup2", text.getTitle() );
		assertEquals( "Global markup2: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	void assetScopeCreationLinksToTheAsset() {
		html.assertElementHasText( "Asset markup: Auto create creation scope", "#component-scope-specified-asset" );

		val text = componentModelService.getComponentModelByNameAndDomain( "asset-markup", page, domain, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( domain, text.getDomain() );
		assertEquals( "asset-markup", text.getName() );
		assertEquals( "Asset markup", text.getTitle() );
		assertEquals( "Asset markup: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	void domainScopeCreation() {
		html.assertElementHasText( "Domain markup: Auto create creation scope", "#component-scope-specified-domain" );

		assertNull( componentModelService.getComponentModelByName( "domain-markup", page ) );

		val text = componentModelService.getComponentModelByNameAndDomain( "domain-markup", null, domain, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( domain, text.getDomain() );
		assertEquals( "domain-markup", text.getName() );
		assertEquals( "Domain markup", text.getTitle() );
		assertEquals( "Domain markup: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
	}

	@Test
	void componentIsCreatedWithTypeSpecified() {
		html.assertElementHasText( "Page rich-text: Auto create creation scope", "#type-specified" );

		val text = componentModelService.getComponentModelByNameAndDomain( "page-rich-text", page, domain, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( domain, text.getDomain() );
		assertEquals( "page-rich-text", text.getName() );
		assertEquals( "Page rich text", text.getTitle() );
		assertEquals( "Page rich-text: Auto create creation scope", text.getContent() );
		assertEquals( TextWebCmsComponentModel.MarkupType.RICH_TEXT, text.getMarkupType() );
	}

	@Test
	void defaultTypeIsContainerIfChildComponentsGetCreated() {
		html.assertElementHasHTML( "container titlecontainer body", "#default-container-type" );

		val container = componentModelService.getComponentModelByNameAndDomain( "page-container", page, domain, ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( domain, container.getDomain() );
		assertEquals( "page-container", container.getName() );
		assertEquals( "Page container", container.getTitle() );

		assertEquals( 2, container.size() );

		val title = container.getMember( "title", TextWebCmsComponentModel.class );
		assertNotNull( title );
		assertEquals( domain, title.getDomain() );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, title.getMarkupType() );
		assertEquals( "container title", title.getContent() );

		val body = container.getMember( "body", TextWebCmsComponentModel.class );
		assertNotNull( body );
		assertEquals( domain, body.getDomain() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, body.getMarkupType() );
		assertEquals( "Body", body.getTitle() );
		assertEquals( "container body", body.getContent() );
	}

	@Test
	void nestedStructureIsReflectedInContainers() {
		html.assertElementHasHTML( "container titlecontainer sub titlecontainer footer text", "#nested-containers" );

		val container = componentModelService.getComponentModelByNameAndDomain( "page-nested-container", page, domain, ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		assertEquals( domain, container.getDomain() );
		assertEquals( "page-nested-container", container.getName() );
		assertEquals( "Page nested container", container.getTitle() );

		assertEquals( 3, container.size() );

		val title = container.getMember( "title", TextWebCmsComponentModel.class );
		assertNotNull( title );
		assertEquals( domain, title.getDomain() );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, title.getMarkupType() );
		assertEquals( "container title", title.getContent() );

		val body = container.getMember( "body", ContainerWebCmsComponentModel.class );
		assertEquals( 2, body.size() );
		assertNotNull( body );
		assertEquals( domain, body.getDomain() );
		val subTitle = body.getMember( "subtitle", TextWebCmsComponentModel.class );
		assertNotNull( subTitle );
		assertEquals( domain, subTitle.getDomain() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, subTitle.getMarkupType() );
		assertEquals( "container sub title", subTitle.getContent() );
		val textContainer = body.getMember( "text", ContainerWebCmsComponentModel.class );
		assertNotNull( textContainer );
		assertEquals( domain, textContainer.getDomain() );
		assertTrue( textContainer.isEmpty() );

		val globalFooter = componentModelService.getComponentModelByNameAndDomain( "footer", null, WebCmsDomain.NONE );
		assertNotNull( globalFooter );
		assertNull( globalFooter.getDomain() );
		val footer = container.getMember( "footer", ContainerWebCmsComponentModel.class );
		assertNotSame( footer, globalFooter );
		assertEquals( domain, footer.getDomain() );
		assertEquals( 1, footer.size() );
		assertNotNull( body );
		val footerText = footer.getMember( "footer-text", TextWebCmsComponentModel.class );
		assertNotNull( footerText );
		assertEquals( domain, footerText.getDomain() );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, footerText.getMarkupType() );
		assertEquals( "container footer text", footerText.getContent() );
	}

	@Test
	void secondRenderYieldsSameOutput() {
		Html secondRender = html( "http://foreach.be/auto-create-creation-scope" );
		secondRender.assertElementHasText( "Page markup: Auto create creation scope", "#no-scope-or-type-specified" );
		secondRender.assertElementHasText( "Global markup: Auto create creation scope", "#creation-scope-specified" );
		secondRender.assertElementHasText( "Global markup2: Auto create creation scope", "#component-scope-specified" );
		secondRender.assertElementHasText( "Asset markup: Auto create creation scope", "#component-scope-specified-asset" );
		secondRender.assertElementHasText( "Domain markup: Auto create creation scope", "#component-scope-specified-domain" );
		secondRender.assertElementHasText( "Page rich-text: Auto create creation scope", "#type-specified" );
		secondRender.assertElementHasHTML( "container titlecontainer body", "#default-container-type" );
		secondRender.assertElementHasHTML( "container titlecontainer sub titlecontainer footer text", "#nested-containers" );
	}

	// metadata can be set (through json?)
}
