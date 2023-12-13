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

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsPublication
{
	@Test
	public void defaultValues() {
		WebCmsPublication publication = new WebCmsPublication();
		verifyDefaultValues( publication );
		verifyDefaultValues( WebCmsPublication.builder().build() );
		verifyDefaultValues( publication.toDto() );
		verifyDefaultValues( publication.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsPublication publication ) {
		assertNull( publication.getId() );
		assertNull( publication.getNewEntityId() );
		assertTrue( publication.isNew() );
		assertNotNull( publication.getObjectId() );
		assertNull( publication.getPublicationKey() );
		assertNull( publication.getPublicationType() );
		assertNull( publication.getName() );
		assertNull( publication.getCreatedBy() );
		assertNull( publication.getCreatedDate() );
		assertNull( publication.getLastModifiedBy() );
		assertNull( publication.getLastModifiedDate() );
		assertEquals( 1000, publication.getSortIndex() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsPublicationType publicationType = WebCmsPublicationType.builder().id( 1L ).build();

		WebCmsPublication publication = WebCmsPublication.builder()
		                                                 .newEntityId( 123L )
		                                                 .publicationKey( "publication-key" )
		                                                 .publicationType( publicationType )
		                                                 .name( "my-publication" )
		                                                 .createdBy( "john" )
		                                                 .createdDate( timestamp )
		                                                 .lastModifiedBy( "josh" )
		                                                 .build();

		assertNull( publication.getId() );
		assertEquals( Long.valueOf( 123L ), publication.getNewEntityId() );
		assertSame( publicationType, publication.getPublicationType() );
		assertEquals( "publication-key", publication.getPublicationKey() );
		assertEquals( "my-publication", publication.getName() );
		assertEquals( "john", publication.getCreatedBy() );
		assertEquals( timestamp, publication.getCreatedDate() );
		assertEquals( "josh", publication.getLastModifiedBy() );
		assertNull( publication.getLastModifiedDate() );
		assertEquals( 1000, publication.getSortIndex() );

		WebCmsPublication other = publication.toBuilder()
		                                     .objectId( "my-asset" )
		                                     .id( 333L )
		                                     .lastModifiedDate( timestamp )
		                                     .build();
		assertNotSame( publication, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:asset:publication:my-asset", other.getObjectId() );
		assertEquals( "publication-key", publication.getPublicationKey() );
		assertSame( publicationType, publication.getPublicationType() );
		assertEquals( "my-publication", publication.getName() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( timestamp, other.getLastModifiedDate() );
		assertEquals( 1000, other.getSortIndex() );
	}
}
