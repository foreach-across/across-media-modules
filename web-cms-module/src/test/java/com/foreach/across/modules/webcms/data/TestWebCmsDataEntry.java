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

package com.foreach.across.modules.webcms.data;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsDataEntry
{
	@Test(expected = IllegalArgumentException.class)
	public void dataMustNotBeNull() {
		new WebCmsDataEntry( "publicationKey", null );
	}

	@Test
	public void singleValueData() {
		WebCmsDataEntry entry = new WebCmsDataEntry( "publicationKey", "some data" );
		assertTrue( entry.isSingleValue() );
		assertEquals( "some data", entry.getSingleValue() );
		assertFalse( entry.isMapData() );
		assertFalse( entry.isCollectionData() );
	}

	@Test
	public void mapDataShouldBeKeptAsLinkedHashMap() {
		Map<String, String> data = new LinkedHashMap<>();
		data.put( "publication", "one" );
		data.put( "article", "two" );

		WebCmsDataEntry entry = new WebCmsDataEntry( "mykey", data );
		List<String> keysInOrder = new ArrayList<>( 2 );
		entry.getMapData()
		     .forEach( ( key, value ) -> keysInOrder.add( key ) );

		assertEquals( Arrays.asList( "publication", "article" ), keysInOrder );
	}

	@Test
	public void dataWithoutParent() {
		Map<String, String> data = new HashMap<>();
		data.put( "1", "2" );

		WebCmsDataEntry entry = new WebCmsDataEntry( "mykey", data );
		assertEquals( "mykey", entry.getKey() );
		assertFalse( entry.hasParent() );
		assertNull( entry.getParent() );
		assertNull( entry.getParentKey() );
		assertEquals( data, entry.getMapData() );
	}

	@Test
	public void dataWithParent() {
		Map<String, String> data = new HashMap<>();
		data.put( "1", "2" );

		WebCmsDataEntry entry = new WebCmsDataEntry( "mykey", new WebCmsDataEntry( "parentKey", Collections.emptyMap() ), data );
		assertEquals( "mykey", entry.getKey() );
		assertTrue( entry.hasParent() );
		assertEquals( "parentKey", entry.getParentKey() );
		assertEquals( data, entry.getMapData() );
	}

	@Test
	public void defaultActionIsCreateUpdate() {
		assertEquals( WebCmsDataImportAction.CREATE_OR_UPDATE, new WebCmsDataEntry( "data", Collections.emptyMap() ).getImportAction() );
		assertEquals( WebCmsDataImportAction.CREATE_OR_UPDATE, new WebCmsDataEntry( "data", Collections.emptyList() ).getImportAction() );
	}

	@Test
	public void actionSpecifiedInMapDataSetsTheEntryAction() {
		assertEquals(
				WebCmsDataImportAction.DELETE,
				new WebCmsDataEntry( "data", Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) ).getImportAction()
		);
	}

	@Test
	public void actionKeyShouldBeRemovedFromMapData() {
		WebCmsDataEntry entry = new WebCmsDataEntry( "data", Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) );
		assertEquals( WebCmsDataImportAction.DELETE, entry.getImportAction() );

		assertEquals( Collections.emptyMap(), entry.getMapData() );
	}

	@Test
	public void parentActionIsUsedIfNotSpecifiedInMapData() {
		WebCmsDataEntry parent = new WebCmsDataEntry( "parent", Collections.emptyList() );
		parent.setImportAction( WebCmsDataImportAction.REPLACE );

		assertEquals(
				WebCmsDataImportAction.REPLACE,
				new WebCmsDataEntry( "data", parent, Collections.emptyList() ).getImportAction()
		);
	}

	@Test
	public void actionSpecifiedWinsOverParentAction() {
		WebCmsDataEntry parent = new WebCmsDataEntry( "parent", Collections.emptyList() );
		parent.setImportAction( WebCmsDataImportAction.REPLACE );

		assertEquals(
				WebCmsDataImportAction.DELETE,
				new WebCmsDataEntry( "data", parent, Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) ).getImportAction()
		);
	}

	@Test
	public void entryLevelActionCanBeModifiedAfterEntryCreation() {
		WebCmsDataEntry data = new WebCmsDataEntry( "data", Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) );
		data.setImportAction( WebCmsDataImportAction.CREATE );

		assertEquals( WebCmsDataImportAction.CREATE, data.getImportAction() );
	}
}
