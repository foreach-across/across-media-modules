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

package com.foreach.across.modules.webcms.domain.domain.web;

import com.foreach.across.modules.adminweb.menu.AdminMenu;
import com.foreach.across.modules.adminweb.menu.AdminMenuEvent;
import com.foreach.across.modules.adminweb.ui.AdminWebLayoutTemplate;
import com.foreach.across.modules.bootstrapui.components.builder.NavComponentBuilder;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.domain.*;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.foreach.across.modules.webcms.config.icons.WebCmsIcons.webCmsIcons;

/**
 * Builds the admin domain selector in case per-domain management is active.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
class WebCmsDomainNavBuilder
{
	private final WebCmsMultiDomainConfiguration multiDomainConfiguration;
	private final WebCmsDomainRepository domainRepository;

	private final WebCmsMultiDomainAdminUiService adminUiService;

	@EventListener
	void buildDomainSelector( AdminMenuEvent event ) {
		if ( !multiDomainConfiguration.isDisabled() && !multiDomainConfiguration.isDomainSelectablePerEntity() ) {
			PathBasedMenuBuilder menuBuilder = event.builder()
			                                        .group( "/wcmDomain", "#{webCmsModule.menu.domainNav.switchDomain=Switch domain}" )
			                                        .attribute( NavComponentBuilder.ATTR_ICON, webCmsIcons.menu.domainGroup() )
			                                        .attribute( NavComponentBuilder.ATTR_ICON_ONLY, true )
			                                        .attribute( AdminMenu.ATTR_NAV_POSITION, AdminWebLayoutTemplate.NAVBAR_RIGHT )
			                                        .and();

			WebCmsDomainContext domainContext = WebCmsDomainContextHolder.getWebCmsDomainContext();
			WebCmsDomain currentDomain = domainContext != null ? domainContext.getDomain() : null;

			List<WebCmsDomain> accessibleDomains = new ArrayList<>( adminUiService.getAccessibleDomains( AllowableAction.READ ) );
			boolean noDomainAllowed = accessibleDomains.remove( WebCmsDomain.NONE );

			if ( noDomainAllowed ) {
				PathBasedMenuBuilder.PathBasedMenuItemBuilder itemBuilder
						= menuBuilder.item( "/wcmDomain/no-domain", "#{webCmsModule.menu.domainNav.noDomain=Shared settings}",
						                    "@adminWeb:/?ts=" + System.currentTimeMillis() + "&wcmSelectDomain=no-domain" )
						             .attribute( NavComponentBuilder.ATTR_INSERT_SEPARATOR, NavComponentBuilder.Separator.AFTER )
						             .order( Ordered.HIGHEST_PRECEDENCE );

				if ( WebCmsDomain.isNoDomain( currentDomain ) ) {
					itemBuilder.attribute( NavComponentBuilder.ATTR_ICON, webCmsIcons.menu.selectedDomain() );
				}
			}

			accessibleDomains.forEach(
					domain -> {
						PathBasedMenuBuilder.PathBasedMenuItemBuilder item = menuBuilder
								.item( "/wcmDomain/" + domain.getId(), domain.getName(),
								       "@adminWeb:/?ts=" + System.currentTimeMillis() + "&wcmSelectDomain=" + domain.getDomainKey() );

						if ( domain.equals( currentDomain ) ) {
							item.attribute( NavComponentBuilder.ATTR_ICON, webCmsIcons.menu.selectedDomain() );
							menuBuilder.root( "/" ).title( domain.getName() ).attribute( NavComponentBuilder.ATTR_ICON_ONLY, false );
						}
					}
			);
		}
	}
}
