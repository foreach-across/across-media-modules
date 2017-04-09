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

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsComponent
{
	@Test
	public void defaultValues() {
		WebCmsComponent component = new WebCmsComponent();
		verifyDefaultValues( component );
		verifyDefaultValues( WebCmsComponent.builder().build() );
		verifyDefaultValues( component.toDto() );
		verifyDefaultValues( component.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsComponent component ) {
		assertNull( component.getTitle() );
		assertNull( component.getOwnerObjectId() );
		assertNull( component.getBody() );
		assertNull( component.getMetadata() );

		assertNull( component.getId() );
		assertNull( component.getNewEntityId() );
		assertTrue( component.isNew() );
		assertNotNull( component.getObjectId() );
		assertTrue( component.getObjectId().startsWith( "wcm:component:" ) );
		assertNull( component.getCreatedBy() );
		assertNull( component.getCreatedDate() );
		assertNull( component.getLastModifiedBy() );
		assertNull( component.getLastModifiedDate() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsComponent component = WebCmsComponent.builder()
		                                           .newEntityId( 123L )
		                                           .objectId( "my-asset" )
		                                           .title( "component-title" )
		                                           .body( "component-body" )
		                                           .metadata( "component-metadata" )
		                                           .ownerObjectId( "wcm:asset:boe" )
		                                           .createdBy( "john" )
		                                           .createdDate( timestamp )
		                                           .lastModifiedBy( "josh" )
		                                           .build();

		assertNull( component.getId() );
		assertEquals( Long.valueOf( 123L ), component.getNewEntityId() );
		assertEquals( "wcm:component:my-asset", component.getObjectId() );
		assertEquals( "component-title", component.getTitle() );
		assertEquals( "component-body", component.getBody() );
		assertEquals( "component-metadata", component.getMetadata() );
		assertEquals( "wcm:asset:boe", component.getOwnerObjectId() );
		assertEquals( "john", component.getCreatedBy() );
		assertEquals( timestamp, component.getCreatedDate() );
		assertEquals( "josh", component.getLastModifiedBy() );
		assertNull( component.getLastModifiedDate() );

		WebCmsComponent other = component.toBuilder()
		                                 .id( 333L )
		                                 .lastModifiedDate( timestamp )
		                                 .build();
		assertNotSame( component, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:component:my-asset", other.getObjectId() );
		assertEquals( "component-title", other.getTitle() );
		assertEquals( "component-body", other.getBody() );
		assertEquals( "component-metadata", other.getMetadata() );
		assertEquals( "wcm:asset:boe", other.getOwnerObjectId() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( timestamp, other.getLastModifiedDate() );
	}
}
