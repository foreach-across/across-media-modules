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

package com.foreach.across.modules.webcms.infrastructure;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.junit.Test;

import static com.foreach.across.modules.webcms.infrastructure.WebCmsUtils.generateCanonicalPath;
import static com.foreach.across.modules.webcms.infrastructure.WebCmsUtils.generateUrlPathSegment;
import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsUtils
{
	@Test(expected = NullPointerException.class)
	public void unableToGeneratePathSegmentForNull() {
		generateUrlPathSegment( null );
	}

	@Test
	public void textToUrlPathSegmentConversion() {
		assertEquals( "", generateUrlPathSegment( "" ) );
		assertEquals( "title", generateUrlPathSegment( "Title" ) );
		assertEquals( "some-day-soon", generateUrlPathSegment( "Some day soon!" ) );
		assertEquals( "very-time-consuming-stuff-happening-here-3-times-h-m-done",
		              generateUrlPathSegment( "VERY   time-consuming stuff,happening|here!!3~timés H&M done  " ) );
		assertEquals( "nested-path-things", generateUrlPathSegment( "/nested/path/things" ) );
	}

	@Test(expected = NullPointerException.class)
	public void unableToGenerateCanonicalPathForNull() {
		generateCanonicalPath( null );
	}

	@Test
	public void rootCanonicalPath() {
		assertEquals( "/", generateCanonicalPath( new WebCmsPage() ) );
	}

	@Test
	public void canonicalPathForPageWithoutParent() {
		assertEquals( "/about", generateCanonicalPath( page().pathSegment( "about" ).build() ) );
		assertEquals( "/my-page", generateCanonicalPath( page().pathSegment( "my-page" ).build() ) );
		assertEquals( "/my/page", generateCanonicalPath( page().pathSegment( "my/page" ).build() ) );
		assertEquals( "/my//page", generateCanonicalPath( page().pathSegment( "my//page" ).build() ) );
	}

	@Test
	public void canonicalPathWhereParentIsRoot() {
		assertEquals(
				"/about",
				generateCanonicalPath(
						page().pathSegment( "about" )
						      .parent( page().canonicalPath( "/" ).build() )
						      .build()
				)
		);
	}

	@Test
	public void canonicalPathWithParent() {
		assertEquals(
				"/pages/about",
				generateCanonicalPath(
						page().pathSegment( "about" )
						      .parent( page().canonicalPath( "/pages" ).build() )
						      .build()
				)
		);
	}

	@Test
	public void generateObjectId() {
		String objectId = WebCmsUtils.generateObjectId( "wcm:asset:page" );
		String other = WebCmsUtils.generateObjectId( "wcm:asset:page" );
		assertNotEquals( objectId, other );
		assertTrue( objectId.startsWith( "wcm:asset:page:" ) );
		assertTrue( other.startsWith( "wcm:asset:page:" ) );
	}

	@Test
	public void isObjectIdForCollection() {
		String objectId = "wcm:asset:test:1561sd65f1sd56";
		assertFalse( WebCmsUtils.isObjectIdForCollection( null, "wcm:asset:test") );
		assertFalse( WebCmsUtils.isObjectIdForCollection( "", "wcm:asset:test") );
		assertTrue( WebCmsUtils.isObjectIdForCollection( objectId, "wcm:asset:test") );
		assertFalse( WebCmsUtils.isObjectIdForCollection( objectId, "wcm:asset:test2") );
		assertFalse( WebCmsUtils.isObjectIdForCollection( "wcm:asset:test1561sd65f1sd56", "wcm:asset:test") );
	}

	@Test
	public void prefixObjectIdForCollection() {
		assertEquals( "wcm:asset:page:test-page", WebCmsUtils.prefixObjectIdForCollection( "test-page", "wcm:asset:page" ) );
		assertEquals( "wcm:asset:page:tp:tp", WebCmsUtils.prefixObjectIdForCollection( "wcm:asset:page:tp:tp", "wcm:asset:page" ) );
	}

	@Test
	public void combineUrlSegments() {
		assertEquals( "/base/page/my-path", WebCmsUtils.combineUrlSegments( "/base/page", "my-path" ) );
		assertEquals( "/base/page/my-path", WebCmsUtils.combineUrlSegments( "/base/page/*", "my-path" ) );
		assertEquals( "/base/page/my-path/*", WebCmsUtils.combineUrlSegments( "/base/page", "my-path/*" ) );
		assertEquals( "/base/my-path", WebCmsUtils.combineUrlSegments( "/base/*", "my-path" ) );
		assertEquals( "/base/my-path/detail", WebCmsUtils.combineUrlSegments( "/base/*/detail", "my-path" ) );
		assertEquals( "/my-path/page/my-path", WebCmsUtils.combineUrlSegments( "/*/page/*", "my-path" ) );
	}

	private WebCmsPage.WebCmsPageBuilder page() {
		return WebCmsPage.builder();
	}

	// todo canonical path from parent not inherited - skip to one above
}
