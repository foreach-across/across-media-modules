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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ITMultiDomainMenuReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsMenuRepository menuRepository;

	@Autowired
	private WebCmsMenuItemRepository menuItemRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsAssetEndpointRepository endpointRepository;

	@Test
	void topNavShouldHaveBeenImported() {
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( "topNav", null );
		assertNotNull( menu );
		assertEquals( "wcm:menu:top-nav", menu.getObjectId() );
		assertEquals( "A top navigation menu not bound to any domain.", menu.getDescription() );
		assertNull( menu.getDomain() );
	}

	@Test
	void topNavForeachBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( "topNav", domain );
		assertNotNull( menu );
		assertEquals( "A top navigation menu bound to be-foreach domain.", menu.getDescription() );
	}

	@Test
	void sideNavForeachBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( "sideNav", domain );
		assertNotNull( menu );
		assertEquals( "A side navigation menu bound to be-foreach domain.", menu.getDescription() );
	}

	@Test
	void topNavForeachDeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByObjectId( "wcm:domain:de-foreach" );
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( "topNav", domain );
		assertNotNull( menu );
		assertEquals( "A top navigation menu bound to de-foreach domain.", menu.getDescription() );
	}

	@Test
	void topNavForeachBeMenuItemsShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" );
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( "topNav", domain );
		Map<String, WebCmsMenuItem> map = new ArrayList<>( menuItemRepository.findAllByMenu( menu ) )
				.stream()
				.collect( Collectors.toMap( WebCmsMenuItem::getPath, Function.identity() ) );

		assertMenuItem( "/home", "Home", "/home", false, 1, map, domain );
		assertMenuItem( "/info", "Information", null, false, 2, map, domain );
	}

	@Test
	void topNavForeachDeMenuItemsShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByObjectId( "wcm:domain:de-foreach" );
		WebCmsMenu menu = menuRepository.findOneByNameAndDomain( "topNav", domain );
		Map<String, WebCmsMenuItem> map = new ArrayList<>( menuItemRepository.findAllByMenu( menu ) )
				.stream()
				.collect( Collectors.toMap( WebCmsMenuItem::getPath, Function.identity() ) );

		assertMenuItem( "/home", "Home", null, false, 0, map, domain );
		assertMenuItem( "/contact", "Contact", "/contact-us", false, 0, map, domain );
	}

	@Test
	void homepageForeachBeShouldHaveMenusImported() {
		WebCmsPage page = pageRepository.findOneByObjectId( "wcm:asset:page:home-be-foreach" );
		assertNotNull( page );
		List<WebCmsMenuItem> items = new ArrayList<>( menuItemRepository.findAllByEndpoint( endpointRepository.findOneByAssetAndDomain( page, page.getDomain() ) ) );
		assertEquals( 2, items.size() );

		Map<String, WebCmsMenuItem> map = items.stream().collect( Collectors.toMap( menuItem -> menuItem.getMenu().getName(), Function.identity() ) );

		assertMenuItem( "/home-be", "Homepage (BE)", null, false, 0, true, map.get( "sideNav" ), page.getDomain() );
		assertMenuItem( "/help/faq", "FAQ", null, false, 10, false, map.get( "topNav" ), domainRepository.findOneByDomainKey( "de-foreach" ) );
	}

	private void assertMenuItem( String path,
	                             String title,
	                             String url,
	                             boolean group,
	                             int sortIndex,
	                             Map<String, WebCmsMenuItem> itemsByPath,
	                             WebCmsDomain domain ) {
		WebCmsMenuItem actual = itemsByPath.get( path );
		assertMenuItem( path, title, url, group, sortIndex, false, actual, domain );
	}

	private void assertMenuItem( String path,
	                             String title,
	                             String url,
	                             boolean group,
	                             int sortIndex,
	                             boolean generated,
	                             WebCmsMenuItem actual,
	                             WebCmsDomain domain ) {
		assertNotNull( actual );
		assertEquals( path, actual.getPath() );
		assertEquals( title, actual.getTitle() );
		assertEquals( url, actual.getUrl() );
		assertEquals( sortIndex, actual.getSortIndex() );
		assertEquals( generated, actual.isGenerated() );
		assertEquals( group, actual.isGroup() );
		assertEquals( domain, actual.getMenu().getDomain() );
	}

}
