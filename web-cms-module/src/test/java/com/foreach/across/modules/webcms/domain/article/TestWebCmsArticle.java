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

import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
public class TestWebCmsArticle
{
	@Test
	public void defaultValues() {
		WebCmsArticle article = new WebCmsArticle();
		verifyDefaultValues( article );
		verifyDefaultValues( WebCmsArticle.builder().build() );
		verifyDefaultValues( article.toDto() );
		verifyDefaultValues( article.toBuilder().build() );
	}

	private void verifyDefaultValues( WebCmsArticle article ) {
		assertNull( article.getId() );
		assertNull( article.getNewEntityId() );
		assertTrue( article.isNew() );
		assertNotNull( article.getObjectId() );
		assertTrue( article.getObjectId().startsWith( "wcm:asset:article:" ) );
		assertNull( article.getPublication() );
		assertNull( article.getArticleType() );
		assertNull( article.getTitle() );
		assertNull( article.getSubTitle() );
		assertNull( article.getDescription() );
		assertNull( article.getName() );
		assertNull( article.getCreatedBy() );
		assertNull( article.getCreatedDate() );
		assertNull( article.getLastModifiedBy() );
		assertNull( article.getLastModifiedDate() );
		assertEquals( 1000, article.getSortIndex() );
	}

	@Test
	public void builderSemantics() {
		Date timestamp = new Date();

		WebCmsPublication publication = WebCmsPublication.builder().id( 1L ).build();
		WebCmsArticleType articleType = WebCmsArticleType.builder().id( 1L ).build();

		WebCmsArticle article = WebCmsArticle.builder()
		                                     .newEntityId( 123L )
		                                     .articleType( articleType )
		                                     .publication( publication )
		                                     .title( "My title" )
		                                     .subTitle( "My subtitle" )
		                                     .description( "My description" )
		                                     .createdBy( "john" )
		                                     .createdDate( timestamp )
		                                     .lastModifiedBy( "josh" )
		                                     .build();

		assertNull( article.getId() );
		assertEquals( Long.valueOf( 123L ), article.getNewEntityId() );
		assertEquals( articleType, article.getArticleType() );
		assertEquals( publication, article.getPublication() );
		assertEquals( "My title", article.getTitle() );
		assertEquals( "My subtitle", article.getSubTitle() );
		assertEquals( "My description", article.getDescription() );
		assertEquals( "My title", article.getName() );
		assertEquals( "john", article.getCreatedBy() );
		assertEquals( timestamp, article.getCreatedDate() );
		assertEquals( "josh", article.getLastModifiedBy() );
		assertNull( article.getLastModifiedDate() );
		assertEquals( 1000, article.getSortIndex() );

		WebCmsArticle other = article.toBuilder()
		                             .objectId( "my-asset" )
		                             .id( 333L )
		                             .lastModifiedDate( timestamp )
		                             .build();
		assertNotSame( article, other );

		assertNull( other.getNewEntityId() );
		assertEquals( Long.valueOf( 333L ), other.getId() );
		assertEquals( "wcm:asset:article:my-asset", other.getObjectId() );
		assertEquals( "My title", article.getName() );
		assertEquals( "john", other.getCreatedBy() );
		assertEquals( timestamp, other.getCreatedDate() );
		assertEquals( "josh", other.getLastModifiedBy() );
		assertEquals( timestamp, other.getLastModifiedDate() );
		assertEquals( 1000, other.getSortIndex() );
	}
}
