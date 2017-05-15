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
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractSingleApplicationIT;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class ITSimplePages extends AbstractSingleApplicationIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Test
	public void customPageIsImported() {
		val page = pageService.findByCanonicalPath( "/custom-page" )
		                      .orElse( null );
		assertNotNull( page );

		assertEquals( "wcm:asset:page:custom-page", page.getObjectId() );
		assertEquals( "Custom page", page.getTitle() );
		assertEquals( "th/test/pages/custom-page", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "custom-page", page.getPathSegment() );
		assertEquals( "/custom-page", page.getCanonicalPath() );
		assertNull( page.getParent() );
	}

	@Test
	public void customPageRendersTheCustomTemplate() {
		Html doc = html( "/custom-page" );
		doc.assertElementHasText( "Custom page", "title" );
		doc.assertElementHasText( "Fully custom page.", "body" );
	}

	@Test
	public void sharedContentComponentShouldExist() {
		val componentModel = componentModelService.getComponentModelByName( "content", null );
		assertNotNull( componentModel );
		assertTrue( componentModel instanceof TextWebCmsComponentModel );

		TextWebCmsComponentModel text = (TextWebCmsComponentModel) componentModel;
		assertEquals( "content", text.getName() );
		assertEquals( "Content", text.getTitle() );
		assertEquals( TextWebCmsComponentModel.MarkupType.PLAIN_TEXT, text.getMarkupType() );
		assertEquals( "Global component: content", text.getContent() );
	}

	@Test
	public void pageWithDefaultTemplateButNoContent() {
		val page = pageService.findByCanonicalPath( "/default-template-no-content" )
		                      .orElse( null );
		assertNotNull( page );

		assertEquals( "wcm:asset:page:default-template-no-content", page.getObjectId() );
		assertEquals( "Default template without content", page.getTitle() );
		assertNull( page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "default-template-no-content", page.getPathSegment() );
		assertEquals( "/default-template-no-content", page.getCanonicalPath() );
		assertNull( page.getParent() );
	}

	@Test
	public void pageWithDefaultTemplateAndSimpleContentComponent() {
		val page = pageService.findByCanonicalPath( "/default-template-with-content" )
		                      .orElse( null );
		assertNotNull( page );

		assertEquals( "wcm:asset:page:default-template-with-content", page.getObjectId() );
		assertEquals( "Default template with content", page.getTitle() );
		assertNull( page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "default-template-with-content", page.getPathSegment() );
		assertEquals( "/default-template-with-content", page.getCanonicalPath() );
		assertNull( page.getParent() );

		val componentModel = componentModelService.getComponentModelByName( "content", page );
		assertNotNull( componentModel );
		assertTrue( componentModel instanceof ContainerWebCmsComponentModel );

		val container = (ContainerWebCmsComponentModel) componentModel;
		assertEquals( "content", container.getName() );
		assertEquals( "Page content", container.getTitle() );
		assertEquals( 1, container.size() );

		TextWebCmsComponentModel text = (TextWebCmsComponentModel) container.getMembers().get( 0 );
		assertEquals( TextWebCmsComponentModel.MarkupType.MARKUP, text.getMarkupType() );
		assertEquals( "Page component: content 1", text.getContent() );
	}

	@Test
	public void pageWithDefaultTemplateRendersOnlyTheContentComponentOnTheAsset() {
		Html doc = html( "/default-template-no-content" );
		doc.assertElementHasText( "Default template without content", "title" );
		doc.assertElementIsEmpty( "body" );

		doc = html( "/default-template-with-content" );
		doc.assertElementHasText( "Default template with content", "title" );
		doc.assertElementHasText( "Page component: content 1", "body" );
	}
}
