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

package com.foreach.across.modules.webcms.domain.menu;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TestWebCmsMenu
{
	@Test
	public void collectionIdValueShouldBeFixed() {
		assertEquals( "wcm:menu", WebCmsMenu.COLLECTION_ID );
	}

	@Test
	public void defaultValues() {
		WebCmsMenu menu = new WebCmsMenu();
		verifyDefaultValues( menu );
		verifyDefaultValues( WebCmsMenu.builder().build() );
		verifyDefaultValues( menu.toDto() );
		verifyDefaultValues( menu.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsMenu menu ) {
		assertNull( menu.getId() );
		assertNull( menu.getNewEntityId() );
		assertTrue( menu.isNew() );
		assertNotNull( menu.getObjectId() );
		assertTrue( menu.getObjectId().startsWith( "wcm:menu:" ) );
		assertNull( menu.getCreatedBy() );
		assertNull( menu.getCreatedDate() );
		assertNull( menu.getLastModifiedBy() );
		assertNull( menu.getLastModifiedDate() );

		assertNull( menu.getName() );
		assertNull( menu.getDescription() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsMenu menu = WebCmsMenu.builder()
		                            .newEntityId( 123L )
		                            .name( "my-menu" )
		                            .objectId( "my-menu" )
		                            .description( "my-description" )
		                            .createdBy( "john" )
		                            .createdDate( timestamp )
		                            .lastModifiedBy( "josh" )
		                            .build();

		assertNull( menu.getId() );
		assertEquals( Long.valueOf( 123L ), menu.getNewEntityId() );
		assertEquals( "wcm:menu:my-menu", menu.getObjectId() );
		assertEquals( "my-menu", menu.getName() );
		assertEquals( "my-description", menu.getDescription() );
		assertEquals( "john", menu.getCreatedBy() );
		assertEquals( timestamp, menu.getCreatedDate() );
		assertEquals( "josh", menu.getLastModifiedBy() );
		assertNull( menu.getLastModifiedDate() );

		WebCmsMenu other = menu.toBuilder()
		                       .id( 333L )
		                       .lastModifiedDate( timestamp )
		                       .description( "my-new-description" )
		                       .build();
		assertNotSame( menu, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:menu:my-menu", other.getObjectId() );
		assertEquals( "my-menu", other.getName() );
		assertEquals( "my-new-description", other.getDescription() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( timestamp, other.getLastModifiedDate() );
	}
}
