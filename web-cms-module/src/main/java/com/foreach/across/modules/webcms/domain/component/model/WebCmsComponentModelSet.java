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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Represents a lookup for {@link WebCmsComponentModel}s that are accessible by name.
 * All components in a single set are expected to be owned by the value {@link #getOwner()}.
 * <p/>
 * A set can have a fetcher function value ({@link #setFetcherFunction(BiFunction)}).  This function
 * will be used to retrieve a component if it is not yet present in the set.  A component name will only
 * ever be looked up once unless it is removed again.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@NoArgsConstructor
@AllArgsConstructor
public class WebCmsComponentModelSet
{
	private static final WebCmsComponentModel NOT_FOUND_MARKER = new WebCmsComponentModel( WebCmsComponent.builder().build() )
	{
		@Override
		public WebCmsComponentModel asComponentTemplate() {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}
	};

	private final Map<String, WebCmsComponentModel> componentsByName = new HashMap<>();

	@Getter
	@Setter
	private WebCmsObject owner;

	/**
	 * Function to be used to fetch a component by name if it is not found.
	 * This function will only be called the first time the component is looked for.
	 */
	@Setter
	private BiFunction<WebCmsObject, String, WebCmsComponentModel> fetcherFunction;

	public WebCmsComponentModelSet( WebCmsObject owner ) {
		this.owner = owner;
	}

	/**
	 * Add a component to the ordered list.
	 * If another component with that name exists, it will be replaced by name but will still be added to the ordered collection.
	 *
	 * @param componentModel to add
	 */
	public void add( WebCmsComponentModel componentModel ) {
		Assert.notNull( componentModel );
		Assert.notNull( componentModel.getName(), "Only component models with a name are allowed in a WebCmsComponentModelSet" );
		put( componentModel.getName(), componentModel );
	}

	/**
	 * Register a component under a specific name in the set.
	 * The actual value of {@link WebCmsComponentModel#getName()} will be ignored.
	 * <p/>
	 * If the model is {@code null}, you are explicitly marking a component as unavailable.
	 * In case of a fetcher function it will not be looked up either.
	 *
	 * @param name           to register the component under
	 * @param componentModel to add
	 */
	public void put( String name, WebCmsComponentModel componentModel ) {
		Assert.notNull( name );
		componentsByName.put( name, componentModel != null ? componentModel : NOT_FOUND_MARKER );
	}

	/**
	 * @return true if the set contains a component with that name
	 */
	public boolean contains( String name ) {
		return get( name ) != null;
	}

	/**
	 * Gets the component with that name.
	 * If a {@link #fetcherFunction} has been specified, it will be used to retrieve the component if it is not yet present.
	 *
	 * @return component or null if not found
	 */
	public WebCmsComponentModel get( String name ) {
		WebCmsComponentModel model = componentsByName.get( name );
		if ( model == null && fetcherFunction != null ) {
			model = fetcherFunction.apply( owner, name );
			componentsByName.put( name, model != null ? model : NOT_FOUND_MARKER );
		}

		return model != NOT_FOUND_MARKER ? model : null;
	}

	/**
	 * Removes the component registered with that name.
	 * Note that if a fetcher function is set, the component will be fetched again.
	 * If you do not want this, set the component for that name explicitly to {@code null}.
	 *
	 * @param name of the component to remove
	 * @return component removed
	 */
	public WebCmsComponentModel remove( String name ) {
		WebCmsComponentModel existing = get( name );

		if ( existing != null ) {
			remove( existing );
		}

		return existing;
	}

	/**
	 * Removes the component model. If the same component model were to be registered under multiple names, all of them would be removed.
	 *
	 * @param componentModel to remove
	 * @return true if at least one entry has been removed
	 */
	public boolean remove( WebCmsComponentModel componentModel ) {
		Assert.notNull( componentModel );

		List<String> namesToRemove = new ArrayList<>();
		componentsByName.forEach( ( key, value ) -> {
			if ( componentModel.equals( value ) ) {
				namesToRemove.add( key );
			}
		} );

		namesToRemove.forEach( componentsByName::remove );
		return !namesToRemove.isEmpty();
	}

	/**
	 * Remove all components from this set.
	 * If a fetcher function was set, components will be looked up again.
	 */
	public void clear() {
		componentsByName.clear();
	}
}
