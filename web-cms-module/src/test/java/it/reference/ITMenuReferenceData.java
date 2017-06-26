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

package it.reference;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import it.AbstractSingleApplicationIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class ITMenuReferenceData extends AbstractSingleApplicationIT
{
	@Autowired
	private WebCmsMenuRepository menuRepository;

	@Autowired
	private WebCmsMenuItemRepository menuItemRepository;

	@Autowired
	private WebCmsAssetEndpointRepository endpointRepository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Test
	public void topNavigationMenuShouldHaveBeenImported() {
		WebCmsMenu topNav = menuRepository.findOneByName( "topNav" );
		assertNotNull( topNav );
		assertEquals( "Top navigation - updated by extension", topNav.getDescription() );
	}

	@Test
	public void sideNavigationMenuShouldHaveBeenImported() {
		WebCmsMenu sideNav = menuRepository.findOneByName( "sideNav" );
		assertNotNull( sideNav );
		assertEquals( "Sidebar navigation", sideNav.getDescription() );
	}

	@Test
	public void deleteMeNavigationMenuShouldHaveBeenDeleted() {
		assertNull( menuRepository.findOneByName( "deleteMeNav" ) );
	}

	@Test
	public void topNavigationItemsWereImportedAndExtended() {
		Map<String, WebCmsMenuItem> map = new ArrayList<>( menuItemRepository.findAllByMenuName( "topNav" ) )
				.stream()
				.collect( Collectors.toMap( WebCmsMenuItem::getPath, Function.identity() ) );

		assertMenuItem( "/categories", "Categories - updated", null, true, 2, map );
		assertMenuItem( "/categories/gossip", "Gossip", "/category/gossip", false, 2, map );
		assertMenuItem( "/categories/news", "News", "/category/news", false, 1, map );
		assertMenuItem( "/home", "Home", "/home", false, 1, map );
		assertNull( map.get( "/categories/deleteme" ) );
	}

	@Test
	public void sidebarNavigationItemsWereImportedAndExtended() {
		Map<String, WebCmsMenuItem> map = new ArrayList<>( menuItemRepository.findAllByMenuName( "sideNav" ) )
				.stream()
				.collect( Collectors.toMap( WebCmsMenuItem::getPath, Function.identity() ) );

		assertMenuItem( "/home", "Home", "/", false, 0, map );
		assertMenuItem( "/custom-sidenav", "Custom SideNav Item", "/custom-sidenav-item", false, 1, map );
	}

	@Test
	public void faqPageShouldHaveMenuItemsAutoCreated() {
		WebCmsPage page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-faq" );
		assertNotNull( page );

		List<WebCmsMenuItem> items = new ArrayList<>( menuItemRepository.findAllByEndpoint( endpointRepository.findOneByAsset( page ) ) );
		assertEquals( 2, items.size() );

		Map<String, WebCmsMenuItem> map = items.stream().collect( Collectors.toMap( menuItem -> menuItem.getMenu().getName(), Function.identity() ) );

		assertMenuItem( "/faq", "Frequently Asked Questions", null, false, 0, true, map.get( "sideNav" ) );
		assertMenuItem( "/help/faq", "FAQ", null, false, 10, false, map.get( "topNav" ) );
	}

	@Test
	public void conditionsPageShouldHaveMenuItemsAutoCreated() {
		WebCmsPage page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-conditions" );
		assertNotNull( page );

		List<WebCmsMenuItem> items = new ArrayList<>( menuItemRepository.findAllByEndpoint( endpointRepository.findOneByAsset( page ) ) );
		assertEquals( 2, items.size() );

		Map<String, WebCmsMenuItem> map = items.stream().collect( Collectors.toMap( menuItem -> menuItem.getMenu().getName(), Function.identity() ) );

		assertMenuItem( "/general-conditions", "General Conditions", "/help/conditions", true, 4, true, map.get( "sideNav" ) );
		assertMenuItem( "/general-conditions", "General Conditions", null, false, 0, false, map.get( "topNav" ) );
	}

	private void assertMenuItem( String path, String title, String url, boolean group, int sortIndex, Map<String, WebCmsMenuItem> itemsByPath ) {
		WebCmsMenuItem actual = itemsByPath.get( path );
		assertMenuItem( path, title, url, group, sortIndex, false, actual );
	}

	private void assertMenuItem( String path, String title, String url, boolean group, int sortIndex, boolean generated, WebCmsMenuItem actual ) {
		assertNotNull( actual );
		assertEquals( path, actual.getPath() );
		assertEquals( title, actual.getTitle() );
		assertEquals( url, actual.getUrl() );
		assertEquals( sortIndex, actual.getSortIndex() );
		assertEquals( generated, actual.isGenerated() );
		assertEquals( group, actual.isGroup() );
	}
}

