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
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractCmsApplicationWithTestDataIT;
import lombok.val;
import modules.test.metadata.ImageWithAltMetadata;
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
		assertEquals( "Simple metadata", text.getTitle() );

		ImageWithAltMetadata metadata = text.getMetadata( ImageWithAltMetadata.class );
		assertNotNull( metadata );
		assertEquals( "hello", metadata.getAltText() );
		WebCmsImage image = metadata.getImage();
		assertNotNull( image );
		assertEquals( "deer.jpg", image.getName() );
	}
}
