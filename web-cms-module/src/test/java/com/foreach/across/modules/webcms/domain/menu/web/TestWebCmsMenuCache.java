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

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.webcms.WebCmsModuleCache;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuCache;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsMenuCache
{
	@Mock
	private WebCmsMenuItemRepository itemRepository;

	@Mock
	private CacheManager cacheManager;

	private ConcurrentMapCache cache = new ConcurrentMapCache( "" );

	@InjectMocks
	private WebCmsMenuCache menuCache;

	@Before
	public void setUp() throws Exception {
		when( cacheManager.getCache( WebCmsModuleCache.MENU ) ).thenReturn( cache );
		menuCache.reloadCache();
	}

	@Test
	public void noItems() {
		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.emptyList() );
		assertNull( cache.get( "myMenu" ) );

		assertEquals( Collections.emptyList(), menuCache.getMenuItems( "myMenu" ) );

		assertEquals( Collections.emptyList(), cache.get( "myMenu", Collection.class ) );
		assertEquals( Collections.emptyList(), menuCache.getMenuItems( "myMenu" ) );

		verify( itemRepository, times( 1 ) ).findAllByMenuName( "myMenu" );
	}

	@Test
	public void singleNonGroupItem() {
		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).url( "item url" ).sortIndex( 2 ).build();
		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.singletonList( item ) );

		Menu expected = new Menu( "/test", "item title" );
		expected.setOrder( 2 );
		expected.setUrl( "item url" );
		expected.setGroup( false );

		List<Menu> actual = new ArrayList<>( menuCache.getMenuItems( "myMenu" ) );
		assertEquals( 1, actual.size() );
		assertMenu( expected, actual.get( 0 ) );

		assertEquals( actual, cache.get( "myMenu", Collection.class ) );
	}

	@Test
	public void itemAndGroupItem() {
		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).url( "item url" ).sortIndex( 2 ).build();
		WebCmsMenuItem group = WebCmsMenuItem.builder().path( "/group" ).title( "group title" ).group( true ).sortIndex( 1 ).build();
		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Arrays.asList( item, group ) );

		Menu expectedItem = new Menu( "/test", "item title" );
		expectedItem.setOrder( 2 );
		expectedItem.setUrl( "item url" );
		expectedItem.setGroup( false );

		Menu expectedGroup = new Menu( "/group", "group title" );
		expectedGroup.setOrder( 1 );
		expectedGroup.setGroup( true );

		List<Menu> actual = new ArrayList<>( menuCache.getMenuItems( "myMenu" ) );
		assertEquals( 2, actual.size() );
		assertMenu( expectedItem, actual.get( 0 ) );
		assertMenu( expectedGroup, actual.get( 1 ) );
	}

	@Test
	public void linkedPageWithoutSpecificUrl() {
		WebCmsPage linkedPage = WebCmsPage.builder().canonicalPath( "canonicalPath" ).published( true ).build();
		WebCmsAssetEndpoint endpoint = WebCmsAssetEndpoint.builder().asset( linkedPage ).build();
		WebCmsUrl primaryUrl = WebCmsUrl.builder().primary( true ).path( "/primary" ).build();
		endpoint.setUrls( Collections.singletonList( primaryUrl ) );

		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).endpoint( endpoint ).build();

		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.singletonList( item ) );

		Menu expected = new Menu( "/test", "item title" );
		expected.setOrder( 0 );
		expected.setUrl( "/primary" );
		expected.setGroup( false );

		List<Menu> actual = new ArrayList<>( menuCache.getMenuItems( "myMenu" ) );
		assertEquals( 1, actual.size() );
		assertMenu( expected, actual.get( 0 ) );
	}

	@Test
	public void unpublishedPageIsDisabled() {
		WebCmsPage linkedPage = WebCmsPage.builder().canonicalPath( "canonicalPath" ).published( false ).build();
		WebCmsAssetEndpoint endpoint = WebCmsAssetEndpoint.builder().asset( linkedPage ).build();
		WebCmsUrl primaryUrl = WebCmsUrl.builder().primary( true ).path( "/primary" ).build();
		endpoint.setUrls( Collections.singletonList( primaryUrl ) );

		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).endpoint( endpoint ).build();

		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.singletonList( item ) );

		Menu expected = new Menu( "/test", "item title" );
		expected.setOrder( 0 );
		expected.setUrl( "/primary" );
		expected.setGroup( false );
		expected.setDisabled( true );

		List<Menu> actual = new ArrayList<>( menuCache.getMenuItems( "myMenu" ) );
		assertEquals( 1, actual.size() );
		assertMenu( expected, actual.get( 0 ) );
	}

	@Test
	public void specificUrlTakesPrecedence() {
		WebCmsPage linkedPage = WebCmsPage.builder().canonicalPath( "canonicalPath" ).published( true ).build();
		WebCmsAssetEndpoint endpoint = WebCmsAssetEndpoint.builder().asset( linkedPage ).build();
		WebCmsUrl primaryUrl = WebCmsUrl.builder().primary( true ).path( "/primary" ).build();
		endpoint.setUrls( Collections.singletonList( primaryUrl ) );

		WebCmsMenuItem item = WebCmsMenuItem.builder().path( "/test" ).title( "item title" ).url( "item url" ).endpoint( endpoint ).build();

		when( itemRepository.findAllByMenuName( "myMenu" ) ).thenReturn( Collections.singletonList( item ) );

		Menu expected = new Menu( "/test", "item title" );
		expected.setOrder( 0 );
		expected.setUrl( "item url" );
		expected.setGroup( false );

		List<Menu> actual = new ArrayList<>( menuCache.getMenuItems( "myMenu" ) );
		assertEquals( 1, actual.size() );
		assertMenu( expected, actual.get( 0 ) );
	}

	private void assertMenu( Menu expected, Menu actual ) {
		assertNull( actual.getParent() );
		assertEquals( expected.getPath(), actual.getPath() );
		assertEquals( expected.getTitle(), actual.getTitle() );
		assertEquals( expected.getUrl(), actual.getUrl() );
		assertEquals( expected.getOrder(), actual.getOrder() );
		assertEquals( expected.isDisabled(), actual.isDisabled() );
	}
}


