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
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuCache;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Hooks into any {@link com.foreach.across.modules.web.events.BuildMenuEvent} and adds the registered
 * {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem}s to it.
 * <p/>
 * Uses the {@link com.foreach.across.modules.webcms.domain.menu.WebCmsMenuCache} for performance purposes.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.webcms.domain.menu.WebCmsMenuCache
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Component
final class WebCmsMenuBuilder
{
	private final WebCmsMenuCache menuCache;

	@EventListener
	void registerWebCmsMenuItems( BuildMenuEvent buildMenuEvent ) {
		PathBasedMenuBuilder builder = buildMenuEvent.builder();

		menuCache.getMenuItems( buildMenuEvent.getMenuName() )
		         .forEach( item -> {
			                   PathBasedMenuBuilder.PathBasedMenuItemBuilder itemBuilder = builder.item( item.getPath(), item.getTitle(), item.getUrl() )
			                                                                                      .group( item.isGroup() )
			                                                                                      .order( item.getOrder() )
			                                                                                      .disable( item.isDisabled() );
			                   item.getAttributes().forEach( itemBuilder::attribute );
		                   }
		         );
	}
}
