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

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsComponentModelHierarchy
{
	@Mock
	private HttpServletRequest request;

	@Mock
	private WebCmsComponentModelService componentModelService;

	@Mock
	private WebCmsMultiDomainService multiDomainService;

	private WebCmsComponentModelHierarchy components;

	private WebCmsComponentModelSet page, asset;

	@Before
	public void setUp() throws Exception {
		components = new WebCmsComponentModelHierarchy();

		page = new WebCmsComponentModelSet();
		asset = new WebCmsComponentModelSet();
	}

	@Test
	public void getComponentsForUnknownScopeReturnsNull() {
		assertNull( components.getComponentsForScope( "some-scope" ) );
	}

	@Test
	public void addAndGetByScopeName() {
		components.registerComponentsForScope( page, "page" );
		assertSame( page, components.getComponentsForScope( "page" ) );

		components.registerComponentsForScope( asset, "asset" );
		assertSame( asset, components.getComponentsForScope( "asset" ) );
	}

	@Test
	public void aliasScope() {
		components.registerComponentsForScope( page, "page" );
		components.registerAliasForScope( "asset", "page" );

		assertSame( page, components.getComponentsForScope( "page" ) );
		assertSame( page, components.getComponentsForScope( "asset" ) );
	}

	@Test
	public void scopesAddedInOrder() {
		assertTrue( components.getScopeNames().isEmpty() );
		assertNull( components.getDefaultScope() );

		components.registerComponentsForScope( page, "page" );
		assertEquals( Collections.singletonList( "page" ), components.getScopeNames() );
		assertEquals( "page", components.getDefaultScope() );

		components.registerComponentsForScope( asset, "asset" );
		assertEquals( Arrays.asList( "page", "asset" ), components.getScopeNames() );
		assertEquals( "asset", components.getDefaultScope() );
	}

	@Test
	public void replacingExistingScopeDoesNotChangeOrder() {
		components.registerComponentsForScope( page, "page" );
		components.registerComponentsForScope( asset, "asset" );

		WebCmsComponentModelSet newComponents = new WebCmsComponentModelSet();
		components.registerComponentsForScope( newComponents, "page" );
		assertEquals( Arrays.asList( "page", "asset" ), components.getScopeNames() );
		assertEquals( "asset", components.getDefaultScope() );
	}

	@Test
	public void aliasesAreNotShownInScopeNames() {
		components.registerComponentsForScope( page, "page" );
		components.registerAliasForScope( "test", "page" );
		components.registerComponentsForScope( asset, "asset" );
		components.registerAliasForScope( "other", "asset" );

		assertEquals( Arrays.asList( "page", "asset" ), components.getScopeNames() );
	}

	@Test
	public void aliasReplacedWithActualSetIsInjectedInLocation() {
		components.registerComponentsForScope( page, "page" );
		components.registerComponentsForScope( asset, "asset" );
		components.registerAliasForScope( "custom", "page" );

		WebCmsComponentModelSet newComponents = new WebCmsComponentModelSet();
		components.registerComponentsForScope( newComponents, "custom" );

		assertEquals( Arrays.asList( "page", "custom", "asset" ), components.getScopeNames() );
		assertSame( newComponents, components.getComponentsForScope( "custom" ) );
	}

	@Test
	public void reorderScopes() {
		WebCmsComponentModelSet global = new WebCmsComponentModelSet();
		components.registerComponentsForScope( global, "global" );
		components.registerComponentsForScope( asset, "asset" );
		components.registerComponentsForScope( page, "page" );
		assertEquals( Arrays.asList( "global", "asset", "page" ), components.getScopeNames() );
		assertEquals( "page", components.getDefaultScope() );

		components.setScopeOrder( "page", "asset", "global" );
		assertEquals( Arrays.asList( "page", "asset", "global" ), components.getScopeNames() );
		assertEquals( "global", components.getDefaultScope() );

		components.registerAliasForScope( "one", "page" );
		components.registerAliasForScope( "two", "global" );
		components.registerAliasForScope( "three", "asset" );

		components.setScopeOrder( "three", "two", "one", "asset" );
		assertEquals( Arrays.asList( "asset", "global", "page" ), components.getScopeNames() );
		assertEquals( "page", components.getDefaultScope() );
	}

	@Test
	public void removeAndContainsScope() {
		assertFalse( components.containsScope( "page" ) );

		components.registerComponentsForScope( page, "page" );
		components.registerComponentsForScope( asset, "asset" );
		assertTrue( components.containsScope( "page" ) );
		assertTrue( components.containsScope( "asset" ) );

		assertTrue( components.removeComponents( page ) );
		assertFalse( components.removeComponents( page ) );
		assertFalse( components.containsScope( "page" ) );
		assertTrue( components.containsScope( "asset" ) );

		assertTrue( components.removeComponents( "asset" ) );
		assertFalse( components.removeComponents( "asset" ) );
		assertFalse( components.containsScope( "page" ) );
		assertFalse( components.containsScope( "asset" ) );
	}

	@Test
	public void removeAndContainsAlias() {
		assertFalse( components.containsScope( "page" ) );

		components.registerComponentsForScope( asset, "asset" );
		components.registerAliasForScope( "page", "asset" );
		assertTrue( components.containsScope( "page" ) );

		assertTrue( components.removeComponents( "page" ) );
		assertFalse( components.containsScope( "page" ) );
		assertTrue( components.containsScope( "asset" ) );

		components.registerAliasForScope( "page", "asset" );
		assertTrue( components.containsScope( "page" ) );
		assertTrue( components.removeComponents( "asset" ) );
		assertFalse( components.containsScope( "page" ) );
		assertFalse( components.containsScope( "asset" ) );
	}

	@Test
	public void globalScope() {
		components.buildGlobalComponentModelSet( componentModelService, multiDomainService );

		val global = components.getComponentsForScope( WebCmsComponentModelHierarchy.GLOBAL );
		assertNotNull( global );
		assertTrue( components.containsScope( WebCmsComponentModelHierarchy.DOMAIN ) );

		global.get( "123" );
		verify( componentModelService ).getComponentModelByNameAndDomain( "123", null, WebCmsDomain.NONE );

		assertEquals( Collections.singletonList( WebCmsComponentModelHierarchy.GLOBAL ), components.getScopeNames() );
	}

	@Test
	public void globalAndSeparateScope() {
		WebCmsDomain domain = WebCmsDomain.builder().id( 123L ).build();
		when( multiDomainService.getCurrentDomainForType( WebCmsComponent.class ) ).thenReturn( domain );

		components.buildGlobalComponentModelSet( componentModelService, multiDomainService );

		val global = components.getComponentsForScope( WebCmsComponentModelHierarchy.GLOBAL );
		assertNotNull( global );
		val domainScope = components.getComponentsForScope( WebCmsComponentModelHierarchy.DOMAIN );
		assertNotNull( domainScope );
		assertNotSame( global, domainScope );

		assertEquals( WebCmsComponentModelHierarchy.DOMAIN, components.getDefaultScope() );

		global.get( "123" );
		verify( componentModelService ).getComponentModelByNameAndDomain( "123", null, WebCmsDomain.NONE );

		domainScope.get( "123" );
		verify( componentModelService ).getComponentModelByNameAndDomain( "123", null, domain );

		assertEquals( Arrays.asList( WebCmsComponentModelHierarchy.GLOBAL, WebCmsComponentModelHierarchy.DOMAIN ), components.getScopeNames() );
	}

	@Test
	public void requestAttribute() {
		components.registerAsRequestAttribute( request );
		verify( request ).setAttribute( WebCmsComponentModelHierarchy.REQUEST_ATTRIBUTE, components );
	}

	@Test
	public void getComponentsOnEmptyHierarchy() {
		assertNull( components.get( "one" ) );
		assertNull( components.get( "one", false ) );
		assertNull( components.getFromScope( "one", "scope" ) );
		assertNull( components.getFromScope( "one", "scope", false ) );
	}

	@Test
	public void getComponents() {
		Model one = new Model( "one" );
		Model two = new Model( "two" );
		Model three = new Model( "three" );

		Model anotherThree = new Model( "four" );
		anotherThree.setName( "three" );

		WebCmsComponentModelSet global = new WebCmsComponentModelSet();
		components.registerComponentsForScope( global, "global" );
		components.registerComponentsForScope( page, "page" );
		components.registerComponentsForScope( asset, "asset" );

		assertNull( components.get( "one" ) );

		global.add( one );
		page.add( two );
		page.add( anotherThree );
		asset.add( three );

		assertSame( one, components.get( "one" ) );
		assertSame( two, components.get( "two" ) );
		assertSame( three, components.get( "three" ) );
		assertSame( one, components.get( "one", true ) );
		assertSame( two, components.get( "two", true ) );
		assertSame( three, components.get( "three", true ) );

		assertNull( components.get( "one", false ) );
		assertNull( components.get( "two", false ) );
		assertSame( three, components.get( "three", false ) );

		assertNull( components.getFromScope( "one", "asset" ) );
		assertSame( one, components.getFromScope( "one", "asset", true ) );
		assertNull( components.getFromScope( "two", "asset" ) );
		assertSame( two, components.getFromScope( "two", "asset", true ) );
		assertSame( three, components.getFromScope( "three", "asset" ) );

		assertNull( components.getFromScope( "one", "page" ) );
		assertSame( two, components.getFromScope( "two", "page" ) );
		assertSame( anotherThree, components.getFromScope( "three", "page" ) );

		assertSame( one, components.getFromScope( "one", "global" ) );
		assertNull( components.getFromScope( "two", "global" ) );
		assertNull( components.getFromScope( "three", "global" ) );

		assertNull( components.getFromScope( "one", "page", false ) );
		assertSame( two, components.getFromScope( "two", "page", false ) );
		assertSame( anotherThree, components.getFromScope( "three", "page", false ) );
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
