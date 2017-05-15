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

import lombok.Data;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsDataConversionService
{
	private WebCmsDataConversionService conversionService = new WebCmsDataConversionService();

	@Test
	public void simpleMap() {
		Map<String, Object> values = new HashMap<>();
		values.put( "name", "my-name" );
		values.put( "sortIndex", 10 );

		MyObject dto = new MyObject();
		assertTrue( conversionService.convertToPropertyValues( values, dto ) );
		assertEquals( "my-name", dto.getName() );
		assertEquals( 10, dto.getSortIndex() );

		assertFalse( conversionService.convertToPropertyValues( values, dto ) );
	}

	@Test
	public void nestedObject() {
		Map<String, Object> values = new HashMap<>();
		values.put( "name", "my-name" );
		values.put( "sortIndex", 10 );

		Map<String, Object> nestedValues = new HashMap<>();
		nestedValues.put( "name", "nested name" );
		nestedValues.put( "sortIndex", 7 );
		values.put( "nested", nestedValues );

		MyObject dto = new MyObject();
		dto.setNested( new MyObject() );
		assertTrue( conversionService.convertToPropertyValues( values, dto ) );

		assertEquals( "my-name", dto.getName() );
		assertEquals( 10, dto.getSortIndex() );
		assertNotNull( dto.getNested() );
		assertEquals( "nested name", dto.getNested().getName() );
		assertEquals( 7, dto.getNested().getSortIndex() );

		assertFalse( conversionService.convertToPropertyValues( values, dto ) );
	}

	@Test
	public void nestedMap() {
		Map<String, Object> values = new HashMap<>();
		values.put( "name", "my-name" );
		values.put( "sortIndex", 10 );

		Map<String, Object> nestedValues = new HashMap<>();
		nestedValues.put( "name", "nested name" );
		nestedValues.put( "sortIndex", 7 );
		values.put( "children", nestedValues );

		MyObject dto = new MyObject();
		assertTrue( conversionService.convertToPropertyValues( values, dto ) );
		assertEquals( "my-name", dto.getName() );
		assertEquals( 10, dto.getSortIndex() );
		assertNotNull( dto.getChildren() );
		assertEquals( "nested name", dto.getChildren().get( "name" ) );
		assertEquals( "7", dto.getChildren().get( "sortIndex" ) );

		assertFalse( conversionService.convertToPropertyValues( values, dto ) );
	}

	@Data
	static class MyObject
	{
		private String name;
		private int sortIndex;
		private MyObject nested;
		private Map<String, String> children;
	}
}
