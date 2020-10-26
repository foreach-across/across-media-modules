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

package com.foreach.across.modules.webcms.domain.component;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsComponentType
{
	@Test
	public void collectionIdValueShouldBeFixed() {
		assertEquals( "wcm:type:component", WebCmsComponentType.COLLECTION_ID );
	}

	@Test
	public void defaultValues() {
		WebCmsComponentType componentType = new WebCmsComponentType();
		verifyDefaultValues( componentType );
		verifyDefaultValues( WebCmsComponentType.builder().build() );
		verifyDefaultValues( componentType.toDto() );
		verifyDefaultValues( componentType.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsComponentType componentType ) {
		assertEquals( "component", componentType.getObjectType() );
		assertNull( componentType.getId() );
		assertNull( componentType.getNewEntityId() );
		assertTrue( componentType.isNew() );
		assertNotNull( componentType.getObjectId() );
		assertNull( componentType.getTypeKey() );
		assertNull( componentType.getName() );
		assertNull( componentType.getCreatedBy() );
		assertNull( componentType.getCreatedDate() );
		assertNull( componentType.getLastModifiedBy() );
		assertNull( componentType.getLastModifiedDate() );

		assertNull( componentType.getDescription() );
		assertTrue( componentType.getAttributes().isEmpty() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsComponentType componentType = WebCmsComponentType.builder()
		                                                       .newEntityId( 123L )
		                                                       .typeKey( "componentType-key" )
		                                                       .name( "my-componentType" )
		                                                       .description( "my-description" )
		                                                       .createdBy( "john" )
		                                                       .createdDate( timestamp )
		                                                       .lastModifiedBy( "josh" )
		                                                       .attribute( "profile", "test" )
		                                                       .build();

		assertNull( componentType.getId() );
		assertEquals( Long.valueOf( 123L ), componentType.getNewEntityId() );
		assertEquals( "componentType-key", componentType.getTypeKey() );
		assertEquals( "my-componentType", componentType.getName() );
		assertEquals( "my-description", componentType.getDescription() );
		assertEquals( "john", componentType.getCreatedBy() );
		assertEquals( timestamp, componentType.getCreatedDate() );
		assertEquals( "josh", componentType.getLastModifiedBy() );
		assertEquals( "test", componentType.getAttributes().get( "profile" ) );
		assertNull( componentType.getLastModifiedDate() );

		WebCmsComponentType other = componentType.toBuilder()
		                                         .id( 333L )
		                                         .objectId( "my-type" )
		                                         .lastModifiedDate( timestamp )
		                                         .build();
		assertNotSame( componentType, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:type:component:my-type", other.getObjectId() );
		assertEquals( "componentType-key", componentType.getTypeKey() );
		assertEquals( "my-componentType", componentType.getName() );
		assertEquals( "my-description", componentType.getDescription() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( "test", componentType.getAttributes().get( "profile" ) );
		assertEquals( timestamp, other.getLastModifiedDate() );
	}
}
