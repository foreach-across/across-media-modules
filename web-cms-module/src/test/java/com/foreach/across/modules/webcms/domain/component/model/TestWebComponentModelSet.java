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

package com.foreach.across.modules.webcms.domain.component.model;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebComponentModelSet
{
	private WebComponentModelSet components;

	private Model noName, one, two, three;

	@Before
	public void setUp() throws Exception {
		components = new WebComponentModelSet();

		noName = new Model( null );
		one = new Model( "one" );
		two = new Model( "two" );
		three = new Model( "three" );

		assertEquals( "Test model class not equal - parent changed?", noName, noName );
		assertEquals( "Test model class not equal - parent changed?", one, one );
	}

	@Test
	public void defaultValues() {
		assertNull( components.getScopeName() );
		assertNull( components.getParent() );
		assertFalse( components.hasOrderedComponents() );
		assertEquals( 0, components.getOrderedCount() );
		assertEquals( Collections.emptyList(), components.getOrdered() );
	}

	@Test
	public void defaultAdd() {
		components.add( noName );
		assertEquals( 1, components.getOrderedCount() );
		assertTrue( components.hasOrderedComponents() );
		assertEquals( Collections.singletonList( noName ), components.getOrdered() );
		assertNull( components.get( null ) );

		components.add( one );
		assertEquals( one, components.get( "one" ) );

		components.add( two );
		assertEquals( two, components.get( "two" ) );

		assertEquals( 3, components.getOrderedCount() );
		assertEquals( Arrays.asList( noName, one, two ), components.getOrdered() );
	}

	@Test
	public void addMultipleByName() {
		components.add( one );

		Model otherOne = new Model( "one" );
		otherOne.setObjectId( "otherOne" );
		components.add( otherOne );

		assertEquals( otherOne, components.get( "one" ) );
		assertEquals( Arrays.asList( one, otherOne ), components.getOrdered() );
	}

	@Test
	public void addToOrderedOnly() {
		components.addToOrderedOnly( one );
		assertEquals( Collections.singletonList( one ), components.getOrdered() );
		assertNull( components.get( "one" ) );
	}

	@Test
	public void addByNameOnly() {
		components.addByNameOnly( one );
		assertFalse( components.hasOrderedComponents() );
		assertEquals( one, components.get( "one" ) );
	}

	@Test
	public void replace() {
		assertNull( components.replace( "one", two ) );
		assertNull( components.get( "two" ) );
		assertFalse( components.hasOrderedComponents() );

		components.add( one );
		components.add( three );
		assertEquals( one, components.replace( "one", two ) );

		assertNull( components.get( "one" ) );
		assertEquals( two, components.get( "two" ) );
		assertEquals( Arrays.asList( two, three ), components.getOrdered() );
	}

	@Test
	public void addAfter() {
		assertFalse( components.addAfter( two, "one" ) );

		components.add( one );
		assertTrue( components.addAfter( two, "one" ) );
		assertNull( components.get( "two" ) );

		assertTrue( components.addAfter( three, one ) );
		assertNull( components.get( "three" ) );

		assertEquals( Arrays.asList( one, three, two ), components.getOrdered() );
	}

	@Test
	public void addBefore() {
		assertFalse( components.addBefore( two, "one" ) );

		components.add( one );
		assertTrue( components.addBefore( two, "one" ) );
		assertNull( components.get( "two" ) );

		assertTrue( components.addBefore( three, one ) );
		assertNull( components.get( "three" ) );

		assertEquals( Arrays.asList( two, three, one ), components.getOrdered() );
	}

	@Test
	public void remove() {
		assertNull( components.remove( "one" ) );
		assertFalse( components.remove( one ) );

		components.add( one );
		components.add( two );
		components.add( three );
		components.addToOrderedOnly( three );
		assertEquals( Arrays.asList( one, two, three, three ), components.getOrdered() );

		assertEquals( one, components.remove( "one" ) );
		assertNull( components.get( "one" ) );
		assertEquals( Arrays.asList( two, three, three ), components.getOrdered() );

		assertTrue( components.remove( three ) );
		assertNull( components.get( "three" ) );
		assertEquals( Collections.singletonList( two ), components.getOrdered() );
	}

	private class Model extends WebComponentModel
	{
		public Model( String name ) {
			setName( name );
			setComponentType( WebCmsComponentType.builder().id( 1L ).build() );
			setObjectId( name );
		}
	}
}
