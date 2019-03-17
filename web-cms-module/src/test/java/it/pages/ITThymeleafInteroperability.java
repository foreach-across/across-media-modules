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
import it.AbstractCmsApplicationWithTestDataIT;
import it.AbstractMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.7
 */
public class ITThymeleafInteroperability extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	private static AbstractMockMvcTest.Html html;
	private static WebCmsPage page;

	@BeforeEach
	void setUp() {
		if ( html == null ) {
			page = pageService.findByCanonicalPath( "/auto-create-thymeleaf-interop" )
			                  .orElse( null );
			verifyPageDoesButNoneOfTheComponentsExist( page );
			html = html( "/auto-create-thymeleaf-interop" );
		}
	}

	private void verifyPageDoesButNoneOfTheComponentsExist( WebCmsPage page ) {
		assertNotNull( page );

		assertEquals( "wcm:asset:page:auto-create-thymeleaf-interop", page.getObjectId() );
		assertEquals( "Auto create and Thymeleaf interop", page.getTitle() );
		assertEquals( "th/test/pages/auto-create/thymeleaf-interop", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "auto-create-thymeleaf-interop", page.getPathSegment() );
		assertEquals( "/auto-create-thymeleaf-interop", page.getCanonicalPath() );
		assertNull( page.getParent() );

		assertTrue( componentModelService.getComponentModelsForOwner( page, null ).isEmpty() );
	}

	@Test
	void componentNotCreatedOnConditionFail() {
		html.assertNoElement( "#not-created" );
		assertNull( componentModelService.getComponentModelByName( "not-created", page ) );
	}

	@Test
	void componentCreateOnConditionMatch() {
		html.assertElementHasText( "should get created", "#should-get-created" );
		assertNotNull( componentModelService.getComponentModelByName( "should-get-created", page, TextWebCmsComponentModel.class ) );
	}

	@Test
	void conditionalCreationOnComponentElement() {
		html.assertElementHasText( "created", "#nested-creation" );
		assertNull( componentModelService.getComponentModelByName( "nested-not-created", page ) );
		TextWebCmsComponentModel text = componentModelService.getComponentModelByName( "nested-created", page, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEquals( "Component created in nested", text.getTitle() );
	}

	@Test
	@DisplayName("AXWCM-114 auto-create on Thymeleaf fragment")
	void fragmentDoesNotImpactComponentAutoCreate() {
		html.assertElementHasText( "We say: hello", "#fragment-created" );
		TextWebCmsComponentModel text = componentModelService.getComponentModelByName( "fragment-created", page, TextWebCmsComponentModel.class );
		assertNotNull( text );
		assertEqualsIgnoreWhitespace( "We say: hello", text.getContent() );
	}

	@Test
	void autoCreateComponentMembersViaFragment() {
		html.assertElementHasText( "We say: hello-containerWaving...Last component", "#container-with-fragments" );
		ContainerWebCmsComponentModel container = componentModelService.getComponentModelByName( "container-with-fragments", page,
		                                                                                         ContainerWebCmsComponentModel.class );
		assertNotNull( container );
		TextWebCmsComponentModel fragmentCreated = container.getMember( "fragment-created", TextWebCmsComponentModel.class );
		assertNotNull( fragmentCreated );
		assertEqualsIgnoreWhitespace( "We say: hello-container", fragmentCreated.getContent() );
		TextWebCmsComponentModel waving = container.getMember( "waving", TextWebCmsComponentModel.class );
		assertNotNull( waving );
		assertEqualsIgnoreWhitespace( "Waving...", waving.getContent() );
		TextWebCmsComponentModel last = container.getMember( "last", TextWebCmsComponentModel.class );
		assertNotNull( last );
		assertEqualsIgnoreWhitespace( "Last component", last.getContent() );
	}
}
