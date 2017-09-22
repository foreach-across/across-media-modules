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

package com.foreach.across.modules.webcms.domain.menu;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;

/**
 * Higher-level API for interacting with {@link WebCmsMenu}.
 * Has notion of the possible current {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomain}.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public interface WebCmsMenuService
{
	/**
	 * Find the menu with the specific name.
	 * Will inspect the domain configuration to determine the domain the menu should be for.
	 * Will use the current domain and fallback to no-domain if allowed for {@link WebCmsMenu}.
	 *
	 * @param menuName name of the menu
	 * @return menu or null if not found
	 */
	WebCmsMenu getMenuByName( String menuName );

	/**
	 * Find the menu with the specific name for the given domain.
	 * Will fallback to no-domain if allowed for {@link WebCmsMenu}.
	 *
	 * @param menuName name of the menu
	 * @param domain   the menu belongs to
	 * @return menu or null if not found
	 */
	WebCmsMenu getMenuByName( String menuName, WebCmsDomain domain );
}
