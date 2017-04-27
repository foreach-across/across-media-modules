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

import com.foreach.across.core.annotations.Event;
import com.foreach.across.modules.web.events.BuildMenuEvent;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Hooks into any {@link com.foreach.across.modules.web.events.BuildMenuEvent} and adds the registered
 * {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem}s to it.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Component
final class WebCmsMenuBuilder
{
	private final WebCmsMenuItemRepository menuItemRepository;

	@Event
	void registerWebCmsMenuItems( BuildMenuEvent buildMenuEvent ) {
		PathBasedMenuBuilder builder = buildMenuEvent.builder();

		menuItemRepository.findAllByMenuName( buildMenuEvent.getMenuName() )
		                  .forEach( item -> {
			                  String url = item.getUrl();

			                  if ( item.getLinkedPage() != null && StringUtils.isEmpty( url ) ) {
				                  url = item.getLinkedPage().getCanonicalPath();
			                  }

			                  builder.item( item.getPath(), item.getTitle(), url )
			                         .group( item.isGroup() )
			                         .order( item.getSortIndex() );
		                  } );
	}
}
