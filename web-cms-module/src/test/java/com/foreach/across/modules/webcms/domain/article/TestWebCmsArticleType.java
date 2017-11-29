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

package com.foreach.across.modules.webcms.domain.article;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsArticleType
{
	@Test
	public void collectionIdValueShouldBeFixed() {
		assertEquals( "wcm:type:article", WebCmsArticleType.COLLECTION_ID );
	}

	@Test
	public void defaultValues() {
		WebCmsArticleType articleType = new WebCmsArticleType();
		verifyDefaultValues( articleType );
		verifyDefaultValues( WebCmsArticleType.builder().build() );
		verifyDefaultValues( articleType.toDto() );
		verifyDefaultValues( articleType.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsArticleType articleType ) {
		assertEquals( "article", articleType.getObjectType() );
		assertNull( articleType.getId() );
		assertNull( articleType.getNewEntityId() );
		assertTrue( articleType.isNew() );
		assertNotNull( articleType.getObjectId() );
		assertNull( articleType.getTypeKey() );
		assertNull( articleType.getName() );
		assertNull( articleType.getCreatedBy() );
		assertNull( articleType.getCreatedDate() );
		assertNull( articleType.getLastModifiedBy() );
		assertNull( articleType.getLastModifiedDate() );
		assertTrue( articleType.getAttributes().isEmpty() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsArticleType articleType = WebCmsArticleType.builder()
		                                                 .newEntityId( 123L )
		                                                 .typeKey( "articleType-key" )
		                                                 .name( "my-articleType" )
		                                                 .createdBy( "john" )
		                                                 .createdDate( timestamp )
		                                                 .lastModifiedBy( "josh" )
		                                                 .attribute( "profile", "test" )
		                                                 .build();

		assertNull( articleType.getId() );
		assertEquals( Long.valueOf( 123L ), articleType.getNewEntityId() );
		assertEquals( "articleType-key", articleType.getTypeKey() );
		assertEquals( "my-articleType", articleType.getName() );
		assertEquals( "john", articleType.getCreatedBy() );
		assertEquals( timestamp, articleType.getCreatedDate() );
		assertEquals( "josh", articleType.getLastModifiedBy() );
		assertEquals( "test", articleType.getAttributes().get( "profile" ) );
		assertNull( articleType.getLastModifiedDate() );

		WebCmsArticleType other = articleType.toBuilder()
		                                     .id( 333L )
		                                     .lastModifiedDate( timestamp )
		                                     .build();
		assertNotSame( articleType, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "articleType-key", articleType.getTypeKey() );
		assertEquals( "my-articleType", articleType.getName() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( "test", articleType.getAttributes().get( "profile" ) );
		assertEquals( timestamp, other.getLastModifiedDate() );
	}
}
