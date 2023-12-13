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

package com.foreach.across.modules.webcms.domain.image.component;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.WebCmsImageRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.AbstractWebCmsComponentModelRenderingTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestImageWebCmsComponentModel extends AbstractWebCmsComponentModelRenderingTest
{
	private WebCmsImage image;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	@Autowired
	private WebCmsComponentRepository componentRepository;

	@Autowired
	public void createImage( WebCmsImageRepository imageRepository ) {
		image = imageRepository.save(
				WebCmsImage.builder()
				           .name( "my-image" )
				           .externalId( "123456" )
				           .build()
		);
	}

	@Test
	public void defaultProperties() {
		val model = componentModelService.createComponentModel( "image", ImageWebCmsComponentModel.class );
		assertTrue( model.hasMetadata() );
		assertFalse( model.hasImageServerKey() );
		assertFalse( model.hasImage() );
		assertNull( model.getImage() );
		assertNull( model.getImageServerKey() );
	}

	@Test
	public void createReadUpdateImage() {
		val model = componentModelService.createComponentModel( "image", ImageWebCmsComponentModel.class );
		model.setImage( image );

		val component = componentModelService.save( model );

		val created = componentModelService.buildModelForComponent(
				componentRepository.findOneByObjectId( component.getObjectId() ).orElse( null ), ImageWebCmsComponentModel.class
		);

		assertEquals( image, created.getImage() );
		assertEquals( "123456", created.getImageServerKey() );

		created.setImage( null );
		componentModelService.save( created );

		val updated = componentModelService.buildModelForComponent(
				componentRepository.findOneByObjectId( component.getObjectId() ).orElse( null ), ImageWebCmsComponentModel.class
		);
		assertNull( created.getImage() );
		assertNull( created.getImageServerKey() );
	}

	@Test
	public void asTemplate() {
		val model = componentModelService.createComponentModel( "image", ImageWebCmsComponentModel.class );
		model.setImage( image );
		assertTrue( model.hasImage() );
		assertTrue( model.hasImageServerKey() );
		assertEquals( image, model.getImage() );
		assertEquals( "123456", model.getImageServerKey() );

		val template = model.asComponentTemplate();
		template.setImage( null );

		assertNotSame( model.getMetadata(), template.getMetadata() );
		assertEquals( image, model.getImage() );
		assertNull( template.getImage() );
	}
}
