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

import com.foreach.across.modules.webcms.domain.WebCmsObject;
import org.junit.Before;
import org.junit.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestWebCmsComponentModelSet
{
	private WebCmsComponentModelSet components;

	private Model noName, one, two, three;

	@Before
	public void setUp() throws Exception {
		components = new WebCmsComponentModelSet();

		noName = new Model( null );
		one = new Model( "one" );
		two = new Model( "two" );
		three = new Model( "three" );

		assertEquals( "Test model class not equal - parent changed?", noName, noName );
		assertEquals( "Test model class not equal - parent changed?", one, one );
	}

	@Test(expected = IllegalArgumentException.class)
	public void nameIsRequiredOnComponentAdd() {
		components.add( noName );
	}

	@Test(expected = IllegalArgumentException.class)
	public void componentIsRequiredOnAdd() {
		components.add( null );
	}

	@Test(expected = NullPointerException.class)
	public void nameIsRequiredOnPut() {
		components.put( null, noName );
	}

	@Test
	public void defaultAdd() {
		components.add( one );
		assertEquals( one, components.get( "one" ) );

		components.add( two );
		assertEquals( two, components.get( "two" ) );
	}

	@Test
	public void addWithSameNameReplacesPrevious() {
		components.add( one );

		Model otherOne = new Model( "one" );
		otherOne.setObjectId( "otherOne" );
		components.add( otherOne );

		assertEquals( otherOne, components.get( "one" ) );
	}

	@Test
	public void putByName() {
		components.put( "one", noName );
		components.put( "two", one );

		assertEquals( noName, components.get( "one" ) );
		assertEquals( one, components.get( "two" ) );

		components.put( "two", null );
		assertNull( components.get( "two" ) );
	}

	@Test
	public void remove() {
		assertNull( components.remove( "one" ) );
		assertFalse( components.remove( one ) );

		components.add( one );
		components.add( two );

		assertEquals( one, components.remove( "one" ) );
		assertNull( components.get( "one" ) );

		assertEquals( two, components.get( "two" ) );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fetcherIsOnlyCalledTheFirstTimeIfResult() {
		BiFunction<WebCmsObject, String, WebCmsComponentModel> fetcher = mock( BiFunction.class );
		components.setFetcherFunction( fetcher );

		when( fetcher.apply( null, "someComponent" ) ).thenReturn( one );
		assertEquals( one, components.get( "someComponent" ) );
		assertEquals( one, components.get( "someComponent" ) );

		verify( fetcher, times( 1 ) ).apply( null, "someComponent" );

		WebCmsObject owner = mock( WebCmsObject.class );
		when( fetcher.apply( owner, "two" ) ).thenReturn( three );

		components.setOwner( owner );

		assertEquals( one, components.get( "someComponent" ) );
		assertEquals( three, components.get( "two" ) );
		assertEquals( three, components.get( "two" ) );
		verify( fetcher, times( 1 ) ).apply( owner, "two" );
		verifyNoMoreInteractions( fetcher );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fetchedIsOnlyCalledTheFirstTimeIfNoResult() {
		BiFunction<WebCmsObject, String, WebCmsComponentModel> fetcher = mock( BiFunction.class );
		components.setFetcherFunction( fetcher );

		when( fetcher.apply( null, "someComponent" ) ).thenReturn( null );
		assertNull( components.get( "someComponent" ) );
		assertNull( components.get( "someComponent" ) );

		verify( fetcher, times( 1 ) ).apply( null, "someComponent" );

		WebCmsObject owner = mock( WebCmsObject.class );
		when( fetcher.apply( owner, "two" ) ).thenReturn( null );

		components.setOwner( owner );

		assertNull( components.get( "someComponent" ) );
		assertNull( components.get( "two" ) );
		assertNull( components.get( "two" ) );
		verify( fetcher, times( 1 ) ).apply( owner, "two" );
		verifyNoMoreInteractions( fetcher );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fetcherIsCalledAgainAfterClearOrRemove() {
		BiFunction<WebCmsObject, String, WebCmsComponentModel> fetcher = mock( BiFunction.class );
		components.setFetcherFunction( fetcher );

		when( fetcher.apply( null, "someComponent" ) ).thenReturn( one );
		assertEquals( one, components.get( "someComponent" ) );
		verify( fetcher, times( 1 ) ).apply( null, "someComponent" );

		components.remove( "someComponent" );
		assertEquals( one, components.get( "someComponent" ) );
		assertEquals( one, components.get( "someComponent" ) );
		verify( fetcher, times( 2 ) ).apply( null, "someComponent" );

		components.clear();
		assertEquals( one, components.get( "someComponent" ) );
		assertEquals( one, components.get( "someComponent" ) );
		verify( fetcher, times( 3 ) ).apply( null, "someComponent" );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void fetcherIsNotCalledOnExplicitValue() {
		BiFunction<WebCmsObject, String, WebCmsComponentModel> fetcher = mock( BiFunction.class );
		components.setFetcherFunction( fetcher );

		components.put( "one", one );
		components.put( "two", null );
		assertEquals( one, components.get( "one" ) );
		assertNull( components.get( "two" ) );

		verifyNoMoreInteractions( fetcher );
	}

	private class Model extends WebCmsComponentModel
	{
		Model( String name ) {
			setName( name );
		}

		@Override
		public WebCmsComponentModel asComponentTemplate() {
			return new Model( getName() );
		}

		@Override
		public boolean isEmpty() {
			return false;
		}
	}
}
