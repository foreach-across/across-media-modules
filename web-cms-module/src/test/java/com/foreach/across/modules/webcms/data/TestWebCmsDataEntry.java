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
	@Test
	public void nullValueIsSeenAsSingleValue() {
		WebCmsDataEntry entry = WebCmsDataEntry.builder().key( "publicationKey" ).build();
		assertTrue( entry.isSingleValue() );
		assertNull( entry.getSingleValue() );
	}

	@Test
	public void singleValueData() {
		WebCmsDataEntry entry = WebCmsDataEntry.builder().key( "publicationKey" ).data( "some data" ).build();
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

		WebCmsDataEntry entry = WebCmsDataEntry.builder().key( "mykey" ).data( data ).build();
		List<String> keysInOrder = new ArrayList<>( 2 );
		entry.getMapData()
		     .forEach( ( key, value ) -> keysInOrder.add( key ) );

		assertEquals( Arrays.asList( "publication", "article" ), keysInOrder );
	}

	@Test
	public void dataWithoutParent() {
		Map<String, String> data = new HashMap<>();
		data.put( "1", "2" );

		WebCmsDataEntry entry = WebCmsDataEntry.builder().key( "mykey" ).data( data ).build();
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

		WebCmsDataEntry entry = WebCmsDataEntry.builder().key( "mykey" )
		                                       .parent( WebCmsDataEntry.builder().key( "parentKey" ).data( Collections.emptyMap() ).build() )
		                                       .data( data )
		                                       .build();
		assertEquals( "mykey", entry.getKey() );
		assertTrue( entry.hasParent() );
		assertEquals( "parentKey", entry.getParentKey() );
		assertEquals( data, entry.getMapData() );
	}

	@Test
	public void defaultActionIsCreateUpdate() {
		assertEquals( WebCmsDataImportAction.CREATE_OR_UPDATE, WebCmsDataEntry.builder().key( "data" ).data( Collections.emptyMap() ).build()
		                                                                      .getImportAction() );
		assertEquals( WebCmsDataImportAction.CREATE_OR_UPDATE, WebCmsDataEntry.builder().key( "data" ).data( Collections.emptyList() ).build()
		                                                                      .getImportAction() );
	}

	@Test
	public void actionSpecifiedInMapDataSetsTheEntryAction() {
		assertEquals(
				WebCmsDataImportAction.DELETE,
				WebCmsDataEntry.builder().key( "data" ).data( Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) ).build()
				               .getImportAction()
		);
	}

	@Test
	public void actionKeyShouldBeRemovedFromMapData() {
		WebCmsDataEntry entry = WebCmsDataEntry.builder().key( "data" ).data( Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) )
		                                       .build();
		assertEquals( WebCmsDataImportAction.DELETE, entry.getImportAction() );

		assertEquals( Collections.emptyMap(), entry.getMapData() );
	}

	@Test
	public void parentActionIsUsedIfNotSpecifiedInMapData() {
		WebCmsDataEntry parent = WebCmsDataEntry.builder().key( "parent" ).data( Collections.emptyList() ).build();
		parent.setImportAction( WebCmsDataImportAction.REPLACE );

		assertEquals(
				WebCmsDataImportAction.REPLACE,
				WebCmsDataEntry.builder().key( "data" ).parent( parent ).data( Collections.emptyList() ).build().getImportAction()
		);
	}

	@Test
	public void actionSpecifiedWinsOverParentAction() {
		WebCmsDataEntry parent = WebCmsDataEntry.builder().key( "parent" ).data( Collections.emptyList() ).build();
		parent.setImportAction( WebCmsDataImportAction.REPLACE );

		assertEquals(
				WebCmsDataImportAction.DELETE,
				WebCmsDataEntry.builder().key( "data" ).parent( parent ).data( Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) )
				               .build().getImportAction()
		);
	}

	@Test
	public void entryLevelActionCanBeModifiedAfterEntryCreation() {
		WebCmsDataEntry data = WebCmsDataEntry.builder().key( "data" ).data( Collections.singletonMap( WebCmsDataImportAction.ATTRIBUTE_NAME, "delete" ) )
		                                      .build();
		data.setImportAction( WebCmsDataImportAction.CREATE );

		assertEquals( WebCmsDataImportAction.CREATE, data.getImportAction() );
	}

	@Test
	public void getLocation() {
		assertEquals( "/", WebCmsDataEntry.builder().data( "data" ).build().getLocation() );
		assertEquals( "/", WebCmsDataEntry.builder().key( "<root>" ).data( "data" ).build().getLocation() );
		assertEquals( "/<map>", WebCmsDataEntry.builder().data( Collections.emptyMap() ).build().getLocation() );
		assertEquals( "/<list>", WebCmsDataEntry.builder().data( Collections.emptyList() ).build().getLocation() );
		assertEquals( "/key", WebCmsDataEntry.builder().key( "key" ).data( "data" ).build().getLocation() );

		WebCmsDataEntry parentWithKey = WebCmsDataEntry.builder().key( "parent" ).data( "parentData" ).build();
		WebCmsDataEntry parentMapWithoutKey = WebCmsDataEntry.builder().parent( parentWithKey ).data( Collections.emptyMap() ).build();
		WebCmsDataEntry parentListWithoutKey = WebCmsDataEntry.builder().parent( parentMapWithoutKey ).data( Collections.emptyList() ).build();

		assertEquals( "/parent/key", WebCmsDataEntry.builder().key( "key" ).parent( parentWithKey ).data( "data" ).build().getLocation() );
		assertEquals( "/parent/<map>/key", WebCmsDataEntry.builder().key( "key" ).parent( parentMapWithoutKey ).data( "data" ).build().getLocation() );
		assertEquals( "/parent/<map>/<list>/key", WebCmsDataEntry.builder().key( "key" ).parent( parentListWithoutKey ).data( "data" ).build().getLocation() );
	}
}
