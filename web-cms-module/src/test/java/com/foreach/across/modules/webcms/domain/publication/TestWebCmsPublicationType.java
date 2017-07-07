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

package com.foreach.across.modules.webcms.domain.publication;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsPublicationType
{
	@Test
	public void collectionIdValueShouldBeFixed() {
		assertEquals( "wcm:type:publication", WebCmsPublicationType.COLLECTION_ID );
	}

	@Test
	public void defaultValues() {
		WebCmsPublicationType publicationType = new WebCmsPublicationType();
		verifyDefaultValues( publicationType );
		verifyDefaultValues( WebCmsPublicationType.builder().build() );
		verifyDefaultValues( publicationType.toDto() );
		verifyDefaultValues( publicationType.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsPublicationType publicationType ) {
		assertEquals( "publication", publicationType.getObjectType() );
		assertNull( publicationType.getId() );
		assertNull( publicationType.getNewEntityId() );
		assertTrue( publicationType.isNew() );
		assertNull( publicationType.getObjectId() );
		assertNull( publicationType.getTypeKey() );
		assertNull( publicationType.getName() );
		assertNull( publicationType.getCreatedBy() );
		assertNull( publicationType.getCreatedDate() );
		assertNull( publicationType.getLastModifiedBy() );
		assertNull( publicationType.getLastModifiedDate() );
		assertTrue( publicationType.getAttributes().isEmpty() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsPublicationType publicationType = WebCmsPublicationType.builder()
		                                                             .newEntityId( 123L )
		                                                             .typeKey( "publicationType-key" )
		                                                             .name( "my-publicationType" )
		                                                             .createdBy( "john" )
		                                                             .createdDate( timestamp )
		                                                             .lastModifiedBy( "josh" )
		                                                             .attribute( "profile", "test" )
		                                                             .build();

		assertNull( publicationType.getId() );
		assertEquals( Long.valueOf( 123L ), publicationType.getNewEntityId() );
		assertEquals( "wcm:type:publication:publicationType-key", publicationType.getObjectId() );
		assertEquals( "publicationType-key", publicationType.getTypeKey() );
		assertEquals( "my-publicationType", publicationType.getName() );
		assertEquals( "john", publicationType.getCreatedBy() );
		assertEquals( timestamp, publicationType.getCreatedDate() );
		assertEquals( "josh", publicationType.getLastModifiedBy() );
		assertEquals( "test", publicationType.getAttributes().get( "profile" ) );
		assertNull( publicationType.getLastModifiedDate() );

		WebCmsPublicationType other = publicationType.toBuilder()
		                                             .id( 333L )
		                                             .objectId( "my-type" )
		                                             .lastModifiedDate( timestamp )
		                                             .build();
		assertNotSame( publicationType, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:type:publication:my-type", other.getObjectId() );
		assertEquals( "publicationType-key", publicationType.getTypeKey() );
		assertEquals( "my-publicationType", publicationType.getName() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( "test", publicationType.getAttributes().get( "profile" ) );
		assertEquals( timestamp, other.getLastModifiedDate() );
	}
}
