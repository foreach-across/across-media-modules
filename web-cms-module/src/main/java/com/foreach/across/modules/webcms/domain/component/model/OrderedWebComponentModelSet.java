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
 * Represents an ordered collection of {@link WebComponentModel}s that are accessible by name.
 * Components can be both added in order or accessible by name.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NoArgsConstructor
@AllArgsConstructor
public class OrderedWebComponentModelSet
{
	private final List<WebComponentModel> orderedComponents = new ArrayList<>();
	private final Map<String, WebComponentModel> componentsByName = new HashMap<>();

	private final List<WebComponentModel> unmodifiableOrdered = orderedComponents;

	private static final WebComponentModel NOT_FOUND_MARKER = new WebComponentModel()
	{
	};

	@Getter
	@Setter
	private WebCmsObject owner;

	@Getter
	@Setter
	private String scopeName;

	/**
	 * Function to be used to fetch a component by name if it is not found.
	 * This function will only be called the first time the component is looked for.
	 */
	@Setter
	private BiFunction<WebCmsObject, String, WebComponentModel> fetcherFunction;

	public OrderedWebComponentModelSet( String scopeName ) {
		this.scopeName = scopeName;
	}

	public OrderedWebComponentModelSet( WebCmsObject owner, String scopeName ) {
		this.owner = owner;
		this.scopeName = scopeName;
	}

	/**
	 * Add a component to the ordered list.
	 * If another component with that name exists, it will be replaced by name but will still be added to the ordered collection.
	 *
	 * @param componentModel to add
	 */
	public void add( WebComponentModel componentModel ) {
		addByNameOnly( componentModel );
		orderedComponents.add( componentModel );
	}

	/**
	 * Replace the component with the given name by another one.
	 * If no existing component with that name is present, this method will do nothing.
	 *
	 * @param name           of the component to replace
	 * @param componentModel to replace with
	 * @return existing component that was replaced
	 */
	public WebComponentModel replace( String name, WebComponentModel componentModel ) {
		WebComponentModel existing = componentsByName.remove( name );
		if ( existing != null ) {
			addByNameOnly( componentModel );
			orderedComponents.replaceAll( current -> existing.equals( current ) ? componentModel : current );
		}

		return existing;
	}

	/**
	 * Add a component to the ordered collection positioned after another existing component.
	 * Only if there is an existing component will the new component be added.
	 * <p/>
	 * NOTE: Call {@link #addByNameOnly(WebComponentModel)} separately if you also want to register the component by name.
	 *
	 * @param componentToAdd        to add
	 * @param nameOfComponentBefore name of the existing component
	 * @return true if component was added
	 */
	public boolean addAfter( WebComponentModel componentToAdd, String nameOfComponentBefore ) {
		Assert.notNull( nameOfComponentBefore );
		WebComponentModel existing = orderedComponents.stream()
		                                              .filter( c -> nameOfComponentBefore.equals( c.getName() ) )
		                                              .findFirst()
		                                              .orElse( null );

		return addAfter( componentToAdd, existing );
	}

	/**
	 * Add a component to the ordered collection positioned after another existing component.
	 * Only if there is an existing component will the new component be added.
	 * <p/>
	 * NOTE: Call {@link #addByNameOnly(WebComponentModel)} separately if you also want to register the component by name.
	 *
	 * @param componentToAdd  to add
	 * @param componentBefore existing component
	 * @return true if component was added
	 */
	public boolean addAfter( WebComponentModel componentToAdd, WebComponentModel componentBefore ) {
		int index = orderedComponents.indexOf( componentBefore );

		if ( index >= 0 ) {
			if ( index == orderedComponents.size() - 1 ) {
				orderedComponents.add( componentToAdd );
			}
			else {
				orderedComponents.add( index + 1, componentToAdd );
			}

			return true;
		}

		return false;
	}

	/**
	 * Add a component to the ordered collection positioned before another existing component.
	 * Only if there is an existing component will the new component be added.
	 * <p/>
	 * NOTE: Call {@link #addByNameOnly(WebComponentModel)} separately if you also want to register the component by name.
	 *
	 * @param componentToAdd       to add
	 * @param nameOfComponentAfter name of the existing component
	 * @return true if component was added
	 */
	public boolean addBefore( WebComponentModel componentToAdd, String nameOfComponentAfter ) {
		Assert.notNull( nameOfComponentAfter );
		WebComponentModel existing = orderedComponents.stream()
		                                              .filter( c -> nameOfComponentAfter.equals( c.getName() ) )
		                                              .findFirst()
		                                              .orElse( null );

		return addBefore( componentToAdd, existing );
	}

	/**
	 * Add a component to the ordered collection positioned before another existing component.
	 * Only if there is an existing component will the new component be added.
	 * <p/>
	 * NOTE: Call {@link #addByNameOnly(WebComponentModel)} separately if you also want to register the component by name.
	 *
	 * @param componentToAdd to add
	 * @param componentAfter existing component
	 * @return true if component was added
	 */
	public boolean addBefore( WebComponentModel componentToAdd, WebComponentModel componentAfter ) {
		int index = orderedComponents.indexOf( componentAfter );

		if ( index >= 0 ) {
			orderedComponents.add( index, componentToAdd );
			return true;
		}

		return false;
	}

	/**
	 * Add a web component only to the ordered collection, disregarding its name.
	 *
	 * @param componentModel to add
	 */
	public void addToOrderedOnly( WebComponentModel componentModel ) {
		orderedComponents.add( componentModel );
	}

	/**
	 * Add a web component to the set, but don't add it to the ordered collection, only make it available by name.
	 *
	 * @param componentModel to add
	 */
	public void addByNameOnly( WebComponentModel componentModel ) {
		if ( componentModel.getName() != null ) {
			componentsByName.put( componentModel.getName(), componentModel );
		}
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
	public WebComponentModel get( String name ) {
		WebComponentModel model = componentsByName.get( name );
		if ( model == null && fetcherFunction != null ) {
			model = fetcherFunction.apply( owner, name );
			componentsByName.put( name, model != null ? model : NOT_FOUND_MARKER );
		}

		return model != NOT_FOUND_MARKER ? model : null;
	}

	/**
	 * Removes the component registered with that name.  Note that if several different ordered components are
	 * registered with the same name, only the component that is accessible under that name will be removed.
	 *
	 * @param name of the component to remove
	 * @return component removed
	 */
	public WebComponentModel remove( String name ) {
		WebComponentModel existing = get( name );

		if ( existing != null ) {
			remove( existing );
		}

		return existing;
	}

	/**
	 * Removes the component model both from the ordered collection and from the ordered components.
	 * If the same component model were to be registered under multiple names, all of them would be removed.
	 *
	 * @param componentModel to remove
	 * @return true if at least one entry has been removed
	 */
	public boolean remove( WebComponentModel componentModel ) {
		Assert.notNull( componentModel );

		List<String> namesToRemove = new ArrayList<>();
		componentsByName.forEach( ( key, value ) -> {
			if ( componentModel.equals( value ) ) {
				namesToRemove.add( key );
			}
		} );

		namesToRemove.forEach( componentsByName::remove );
		return orderedComponents.removeIf( componentModel::equals ) || !namesToRemove.isEmpty();
	}

	/**
	 * @return immutable ordered components
	 */
	public List<WebComponentModel> getOrdered() {
		return unmodifiableOrdered;
	}

	/**
	 * @return true if the collection of ordered components is not empty
	 */
	public boolean hasOrderedComponents() {
		return !orderedComponents.isEmpty();
	}

	/**
	 * @return number of ordered components
	 */
	public int getOrderedCount() {
		return orderedComponents.size();
	}
}
