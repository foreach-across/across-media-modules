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
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import it.AbstractCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
class ITMenuReferenceData extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsMenuRepository menuRepository;

	@Autowired
	private WebCmsMenuItemRepository menuItemRepository;

	@Autowired
	private WebCmsAssetEndpointRepository endpointRepository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Test
	void topNavigationMenuShouldHaveBeenImported() {
		WebCmsMenu topNav = menuRepository.findOneByNameAndDomain( "topNav", null );
		assertNotNull( topNav );
		assertEquals( "Top navigation - updated by extension", topNav.getDescription() );
		assertNotNull( topNav.getObjectId() );
	}

	@Test
	void sideNavigationMenuShouldHaveBeenImported() {
		WebCmsMenu sideNav = menuRepository.findOneByNameAndDomain( "sideNav", null );
		assertNotNull( sideNav );
		assertEquals( "Sidebar navigation", sideNav.getDescription() );
		assertNotNull( sideNav.getObjectId() );
	}

	@Test
	void deleteMeNavigationMenuShouldHaveBeenDeleted() {
		assertNull( menuRepository.findOneByNameAndDomain( "deleteMeNav", null ) );
	}

	@Test
	void topNavigationItemsWereImportedAndExtended() {
		WebCmsMenu topNav = menuRepository.findOneByNameAndDomain( "topNav", WebCmsDomain.NONE );
		Map<String, WebCmsMenuItem> map = new ArrayList<>( menuItemRepository.findAllByMenu( topNav ) )
				.stream()
				.collect( Collectors.toMap( WebCmsMenuItem::getPath, Function.identity() ) );

		assertMenuItem( "/categories", "Categories - updated", null, true, 2, map );
		assertMenuItem( "/categories/gossip", "Gossip", "/category/gossip", false, 2, map );
		assertMenuItem( "/categories/news", "News", "/category/news", false, 1, map );
		assertMenuItem( "/home", "Home", "/home", false, 1, map );
		assertNull( map.get( "/categories/deleteme" ) );
	}

	@Test
	void sidebarNavigationItemsWereImportedAndExtended() {
		WebCmsMenu sideNav = menuRepository.findOneByNameAndDomain( "sideNav", WebCmsDomain.NONE );
		Map<String, WebCmsMenuItem> map = new ArrayList<>( menuItemRepository.findAllByMenu( sideNav ) )
				.stream()
				.collect( Collectors.toMap( WebCmsMenuItem::getPath, Function.identity() ) );

		assertMenuItem( "/home", "Home", "/", false, 0, map );
		assertMenuItem( "/custom-sidenav", "Custom SideNav Item", "/custom-sidenav-item", false, 1, map );
	}

	@Test
	void faqPageShouldHaveMenuItemsAutoCreated() {
		WebCmsPage page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-faq" );
		assertNotNull( page );

		List<WebCmsMenuItem> items = new ArrayList<>(
				menuItemRepository.findAllByEndpoint( endpointRepository.findOneByAssetAndDomain( page, page.getDomain() ) ) );
		assertEquals( 2, items.size() );

		Map<String, WebCmsMenuItem> map = items.stream().collect( Collectors.toMap( menuItem -> menuItem.getMenu().getName(), Function.identity() ) );

		assertMenuItem( "/faq", "Frequently Asked Questions", null, false, 0, true, map.get( "sideNav" ) );
		assertMenuItem( "/help/faq", "FAQ", null, false, 10, false, map.get( "topNav" ) );
	}

	@Test
	void conditionsPageShouldHaveMenuItemsAutoCreated() {
		WebCmsPage page = pageRepository.findOneByObjectId( "wcm:asset:page:reference-conditions" );
		assertNotNull( page );

		List<WebCmsMenuItem> items = new ArrayList<>(
				menuItemRepository.findAllByEndpoint( endpointRepository.findOneByAssetAndDomain( page, page.getDomain() ) ) );
		assertEquals( 2, items.size() );

		Map<String, WebCmsMenuItem> map = items.stream().collect( Collectors.toMap( menuItem -> menuItem.getMenu().getName(), Function.identity() ) );

		assertMenuItem( "/general-conditions", "General Conditions", "/help/conditions", true, 4, true, map.get( "sideNav" ) );
		assertMenuItem( "/general-conditions", "General Conditions", null, false, 0, false, map.get( "topNav" ) );
	}

	@Test
	void assetShouldHaveBeenLinkedWhenRequested() {
		WebCmsPage page = pageRepository.findOneByObjectId( "wcm:asset:page:contact" );
		assertNotNull( page );

		List<WebCmsMenuItem> items = new ArrayList<>(
				menuItemRepository.findAllByEndpoint( endpointRepository.findOneByAssetAndDomain( page, page.getDomain() ) ) );
		Map<String, WebCmsMenuItem> map = items.stream().collect( Collectors.toMap( menuItem -> menuItem.getMenu().getName(), Function.identity() ) );

		assertEquals( 2, items.size() );
		assertMenuItem( "/contact", "Contact", null, false, 2, false, map.get( "sideNav" ) );

		assertMenuItem( "/my/contact", "My Fancy Contact Page", null, false, 0, true, map.get( "topNav" ) );

		WebCmsPage page2 = pageRepository.findOneByObjectId( "wcm:asset:page:other-contact" );
		assertNotNull( page );

		List<WebCmsMenuItem> items2 = new ArrayList<>(
				menuItemRepository.findAllByEndpoint( endpointRepository.findOneByAssetAndDomain( page2, page2.getDomain() ) ) );
		Map<String, WebCmsMenuItem> map2 = items2.stream().collect( Collectors.toMap( menuItem -> menuItem.getMenu().getName(), Function.identity() ) );
		assertMenuItem( "/my/other/contact", "My Other Fancy Contact Page", null, false, 0, true, map2.get( "topNav" ) );
		assertMenuItem( "/contact2", "Contact2", null, false, 3, false, map2.get( "sideNav" ) );

	}

	@Test
	void navWithObjectIdShouldHaveBeenImported() {
		WebCmsMenu menu = menuRepository.findOneByObjectId( "wcm:menu:nav-object-id" );
		assertNotNull( menu );
		assertEquals( "navWithObjectId", menu.getName() );
		assertEquals( "nav with an objectId", menu.getDescription() );
		assertEquals( domainRepository.findOneByDomainKey( "domain.complex.domain" ), menu.getDomain() );
	}

	@Test
	void myOtherNavShouldHaveBeenImportedAndExtended() {
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( "myOtherNav", WebCmsDomain.NONE );
		assertNotNull( menu );
		assertEquals( "wcm:menu:my-other-nav", menu.getObjectId() );
		assertEquals( "Another nav with an objectId", menu.getDescription() );
		assertNull( menu.getDomain() );
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

