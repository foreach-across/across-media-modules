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

import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public interface WebComponentModelService
{
	/**
	 * Get a single {@link WebComponentModel} by name.
	 *
	 * @param componentName name of the component
	 * @param owner         of the component
	 * @return component or null if not found
	 */
	WebComponentModel getWebComponent( String componentName, WebCmsObject owner );

	/**
	 * Get all {@link WebComponentModel}s owned by a the {@link WebCmsObject}.
	 * The returned set will have all components according to their index order.
	 *
	 * @param object owner
	 * @return set of components
	 */
	OrderedWebComponentModelSet getWebComponentsForOwner( WebCmsObject object );

	WebComponentModel readFromComponent( WebCmsComponent component );

	void writeToComponent( WebComponentModel componentModel, WebCmsComponent component );

	WebCmsComponent save( WebComponentModel componentModel );
}
