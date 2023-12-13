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

package com.foreach.across.modules.webcms.domain.page;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsPage
{
	@Test
	public void defaultValues() {
		WebCmsPage page = new WebCmsPage();
		verifyDefaultValues( page );
		verifyDefaultValues( WebCmsPage.builder().build() );
		verifyDefaultValues( page.toDto() );
		verifyDefaultValues( page.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsPage built ) {
		assertNull( built.getId() );
		assertNull( built.getNewEntityId() );
		assertTrue( built.isNew() );
		assertFalse( built.isPublished() );
		assertNull( built.getPublicationDate() );
		assertNotNull( built.getObjectId() );
		assertTrue( built.getObjectId().startsWith( "wcm:asset:page:" ) );
		assertTrue( built.isPathSegmentGenerated() );
		assertTrue( built.isCanonicalPathGenerated() );
		assertNull( built.getCanonicalPath() );
		assertNull( built.getParent() );
		assertNull( built.getPathSegment() );
		assertNull( built.getTemplate() );
		assertNull( built.getTitle() );
		assertNull( built.getCreatedBy() );
		assertNull( built.getCreatedDate() );
		assertNull( built.getLastModifiedBy() );
		assertNull( built.getLastModifiedDate() );
		assertEquals( 1000, built.getSortIndex() );
	}

	@Test
	public void builderSemantics() {
		WebCmsPage parent = new WebCmsPage();
		Date timestamp = new Date();

		WebCmsPage page = WebCmsPage.builder()
		                            .newEntityId( 123L )
		                            .objectId( "my-asset" )
		                            .title( "some page" )
		                            .parent( parent )
		                            .pathSegment( "path" )
		                            .pathSegmentGenerated( false )
		                            .canonicalPath( "canonicalPath" )
		                            .template( "my-template" )
		                            .createdBy( "john" )
		                            .createdDate( timestamp )
		                            .lastModifiedBy( "josh" )
		                            .build();

		assertNull( page.getId() );
		assertEquals( Long.valueOf( 123L ), page.getNewEntityId() );
		assertEquals( "wcm:asset:page:my-asset", page.getObjectId() );
		assertEquals( "some page", page.getTitle() );
		assertSame( parent, page.getParent() );
		assertEquals( "path", page.getPathSegment() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "canonicalPath", page.getCanonicalPath() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertEquals( "my-template", page.getTemplate() );
		assertEquals( "john", page.getCreatedBy() );
		assertEquals( timestamp, page.getCreatedDate() );
		assertEquals( "josh", page.getLastModifiedBy() );
		assertNull( page.getLastModifiedDate() );
		assertEquals( 1000, page.getSortIndex() );

		WebCmsPage other = page.toBuilder()
		                       .id( 333L )
		                       .pathSegmentGenerated( true )
		                       .canonicalPathGenerated( false )
		                       .lastModifiedDate( timestamp )
		                       .build();
		assertNotSame( page, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:asset:page:my-asset", other.getObjectId() );
		assertEquals( "some page", other.getTitle() );
		assertSame( parent, other.getParent() );
		assertEquals( "path", other.getPathSegment() );
		assertTrue( other.isPathSegmentGenerated() );
		assertEquals( "canonicalPath", other.getCanonicalPath() );
		assertFalse( other.isCanonicalPathGenerated() );
		assertEquals( "my-template", other.getTemplate() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( timestamp, other.getLastModifiedDate() );
		assertEquals( 1000, other.getSortIndex() );
	}
}
