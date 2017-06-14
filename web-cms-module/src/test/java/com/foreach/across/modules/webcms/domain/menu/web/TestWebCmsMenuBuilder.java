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

package com.foreach.across.modules.webcms.domain.menu.web;

import com.foreach.across.modules.web.events.BuildMenuEvent;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsMenuBuilder
{
	@Mock
	private WebCmsMenuItemRepository itemRepository;

	@Mock
	private BuildMenuEvent buildMenuEvent;

	@InjectMocks
	private WebCmsMenuBuilder eventHandler;

	private PathBasedMenuBuilder menuBuilder;

	@Before
	public void setUp() throws Exception {
		menuBuilder = new PathBasedMenuBuilder();
		when( buildMenuEvent.getMenuName() ).thenReturn( "myMenu" );
		when( buildMenuEvent.builder() ).thenReturn( menuBuilder );
	}

	@Test
	public void noItems() {
		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.emptyList() );
		when( itemRepository.findAllByMenuName( "otherMenu" ) ).thenReturn( Collections.singleton( new WebCmsMenuItem() ) );
		eventHandler.registerWebCmsMenuItems( buildMenuEvent );

		assertTrue( menuBuilder.build().isEmpty() );
	}

	@Test
	public void singleNonGroupItem() {
		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).url( "item url" ).sortIndex( 2 ).build();
		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.singletonList( item ) );

		eventHandler.registerWebCmsMenuItems( buildMenuEvent );

		Menu menu = menuBuilder.build();
		assertEquals( 1, menu.getItems().size() );
		Menu child = menu.getItemWithPath( "/test" );
		assertNotNull( child );
		assertEquals( "item title", child.getTitle() );
		assertEquals( "item url", child.getUrl() );
		assertEquals( 2, child.getOrder() );
		assertFalse( child.isGroup() );
		assertFalse( child.isDisabled() );
	}

	@Test
	public void itemAndGroupItem() {
		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).url( "item url" ).sortIndex( 2 ).build();
		WebCmsMenuItem group = WebCmsMenuItem.builder().path( "/group" ).title( "group title" ).group( true ).sortIndex( 1 ).build();
		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Arrays.asList( item, group ) );

		eventHandler.registerWebCmsMenuItems( buildMenuEvent );

		Menu menu = menuBuilder.build();
		assertEquals( 2, menu.getItems().size() );
		Menu childItem = menu.getItemWithPath( "/test" );
		assertNotNull( childItem );
		assertEquals( "item title", childItem.getTitle() );
		assertEquals( "item url", childItem.getUrl() );
		assertEquals( 2, childItem.getOrder() );
		assertFalse( childItem.isGroup() );
		assertFalse( childItem.isDisabled() );

		Menu groupItem = menu.getItemWithPath( "/group" );
		assertNotNull( groupItem );
		assertEquals( "group title", groupItem.getTitle() );
		assertEquals( 1, groupItem.getOrder() );
		assertTrue( groupItem.isGroup() );
		assertFalse( groupItem.isDisabled() );
	}

	@Test
	public void linkedPageWithoutSpecificUrl() {
		WebCmsPage linkedPage = WebCmsPage.builder().canonicalPath( "canonicalPath" ).published( true ).build();
		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).linkedPage( linkedPage ).build();

		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.singletonList( item ) );

		eventHandler.registerWebCmsMenuItems( buildMenuEvent );

		Menu menu = menuBuilder.build();
		assertEquals( 1, menu.getItems().size() );
		Menu child = menu.getItemWithPath( "/test" );
		assertNotNull( child );
		assertEquals( "item title", child.getTitle() );
		assertEquals( "canonicalPath", child.getUrl() );
	}

	@Test
	public void specificUrlTakesPrecedenceOverLinkedPageUrl() {
		WebCmsPage linkedPage = WebCmsPage.builder().canonicalPath( "canonicalPath" ).published( true ).build();
		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).url( "item url" ).linkedPage( linkedPage ).build();

		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.singletonList( item ) );

		eventHandler.registerWebCmsMenuItems( buildMenuEvent );

		Menu menu = menuBuilder.build();
		assertEquals( 1, menu.getItems().size() );
		Menu child = menu.getItemWithPath( "/test" );
		assertNotNull( child );
		assertEquals( "item title", child.getTitle() );
		assertEquals( "item url", child.getUrl() );
	}
}


