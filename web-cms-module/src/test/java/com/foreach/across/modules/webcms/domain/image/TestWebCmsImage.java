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

package com.foreach.across.modules.webcms.domain.image;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
public class TestWebCmsImage
{
	@Test
	public void defaultValues() {
		WebCmsImage image = new WebCmsImage();
		verifyDefaultValues( image );
		verifyDefaultValues( WebCmsImage.builder().build() );
		verifyDefaultValues( image.toDto() );
		verifyDefaultValues( image.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsImage image ) {
		assertNull( image.getId() );
		assertNull( image.getNewEntityId() );
		assertTrue( image.isNew() );
		assertNotNull( image.getObjectId() );
		assertTrue( image.getObjectId().startsWith( "wcm:asset:image:" ) );
		assertNull( image.getName() );
		assertNull( image.getExternalId() );
		assertNull( image.getCreatedBy() );
		assertNull( image.getCreatedDate() );
		assertNull( image.getLastModifiedBy() );
		assertNull( image.getLastModifiedDate() );
		assertEquals( 1000, image.getSortIndex() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsImage image = WebCmsImage.builder()
		                               .newEntityId( 123L )
		                               .name( "my-publication" )
		                               .externalId( "abc123def456ghi789" )
		                               .createdBy( "john" )
		                               .createdDate( timestamp )
		                               .lastModifiedBy( "josh" )
		                               .build();

		assertNull( image.getId() );
		assertEquals( Long.valueOf( 123L ), image.getNewEntityId() );
		assertEquals( "abc123def456ghi789", image.getExternalId() );
		assertEquals( "my-publication", image.getName() );
		assertEquals( "john", image.getCreatedBy() );
		assertEquals( timestamp, image.getCreatedDate() );
		assertEquals( "josh", image.getLastModifiedBy() );
		assertNull( image.getLastModifiedDate() );
		assertEquals( 1000, image.getSortIndex() );

		WebCmsImage other = image.toBuilder()
		                         .objectId( "my-asset" )
		                         .id( 333L )
		                         .lastModifiedDate( timestamp )
		                         .build();
		assertNotSame( image, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:asset:image:my-asset", other.getObjectId() );
		assertEquals( "abc123def456ghi789", image.getExternalId() );
		assertEquals( "my-publication", image.getName() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( timestamp, other.getLastModifiedDate() );
		assertEquals( 1000, other.getSortIndex() );
	}
}
