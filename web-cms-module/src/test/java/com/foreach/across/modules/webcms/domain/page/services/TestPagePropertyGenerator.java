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

package com.foreach.across.modules.webcms.domain.page.services;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationType;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.foreach.across.modules.webcms.domain.page.services.PrepareModificationType.CANONICAL_PATH_GENERATED;
import static com.foreach.across.modules.webcms.domain.page.services.PrepareModificationType.PATH_SEGMENT_GENERATED;
import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestPagePropertyGenerator
{
	private WebCmsPage page;

	private Map<ModificationType, ModificationReport> modifications;

	@Before
	public void setUp() {
		page = new WebCmsPage();
		modifications = null;

		assertTrue( page.isCanonicalPathGenerated() );
		assertTrue( page.isPathSegmentGenerated() );
	}

	@Test
	public void pathSegmentUntouchedIfNotGenerated() {
		page.setPathSegment( "custom segment" );
		page.setTitle( "My title!" );
		page.setPathSegmentGenerated( false );

		prepare();

		assertFalse( modifications.containsKey( PATH_SEGMENT_GENERATED ) );
		assertEquals( "custom segment", page.getPathSegment() );
	}

	@Test
	public void pathSegmentReplacedWithEmptyTitle() {
		page.setPathSegment( "custom segment" );
		prepare();

		val report = modifications.get( PATH_SEGMENT_GENERATED );
		assertEquals( PATH_SEGMENT_GENERATED, report.getModificationType() );
		assertEquals( "custom segment", report.getOldValue() );
		assertEquals( "", report.getNewValue() );
	}

	@Test
	public void pathSegmentGenerated() {
		page.setTitle( "My title!" );
		prepare();

		val report = modifications.get( PATH_SEGMENT_GENERATED );
		assertEquals( PATH_SEGMENT_GENERATED, report.getModificationType() );
		assertNull( report.getOldValue() );
		assertEquals( "my-title", report.getNewValue() );
	}

	@Test
	public void generatedPathSegmentIsSameAsProperty() {
		page.setTitle( "My title!" );
		page.setPathSegment( "my-title" );
		prepare();

		assertFalse( modifications.containsKey( PATH_SEGMENT_GENERATED ) );
		assertEquals( "my-title", page.getPathSegment() );
	}

	@Test
	public void canonicalPathUntouchedIfNotGenerated() {
		page.setCanonicalPath( "/custom/path" );
		page.setPathSegment( "segment" );
		page.setCanonicalPathGenerated( false );
		prepare();

		assertFalse( modifications.containsKey( CANONICAL_PATH_GENERATED ) );
		assertEquals( "/custom/path", page.getCanonicalPath() );
	}

	@Test
	public void canonicalPathGenerated() {
		page.setCanonicalPath( "/custom/path" );
		page.setPathSegment( "segment" );
		page.setPathSegmentGenerated( false );
		prepare();

		val report = modifications.get( CANONICAL_PATH_GENERATED );
		assertEquals( CANONICAL_PATH_GENERATED, report.getModificationType() );
		assertEquals( "/custom/path", report.getOldValue() );
		assertEquals( "/segment", report.getNewValue() );

		assertEquals( "/segment", page.getCanonicalPath() );
	}

	@Test
	public void generatedCanonicalPathIsSameAsProperty() {
		page.setCanonicalPath( "/custom/path" );
		page.setPathSegment( "custom/path" );
		page.setPathSegmentGenerated( false );
		prepare();

		assertFalse( modifications.containsKey( CANONICAL_PATH_GENERATED ) );
		assertEquals( "/custom/path", page.getCanonicalPath() );
	}

	@Test
	public void generationOrder() {
		page.setTitle( "my title" );
		prepare();

		assertEquals( 2, modifications.size() );
		assertTrue( modifications.containsKey( PATH_SEGMENT_GENERATED ) );
		assertTrue( modifications.containsKey( CANONICAL_PATH_GENERATED ) );

		assertEquals( "my-title", page.getPathSegment() );
		assertEquals( "/my-title", page.getCanonicalPath() );
	}

	private void prepare() {
		modifications = new PagePropertyGenerator().prepareForSaving( page );
	}

}
