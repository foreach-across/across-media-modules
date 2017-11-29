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

package com.foreach.across.modules.webcms.domain.component.container;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a collection of other {@link WebCmsComponentModel}s.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class ContainerWebCmsComponentModel extends WebCmsComponentModel
{
	public static final String TYPE_DYNAMIC = "container";
	public static final String TYPE_FIXED = "fixed-container";

	/**
	 * If the {@link com.foreach.across.modules.webcms.domain.component.WebCmsComponentType} has this attribute set to
	 * {@code true} then markup on the container will be used for rendering if it is set.
	 */
	public static final String SUPPORTS_MARKUP_ATTRIBUTE = "supportsMarkup";

	@Getter
	@Setter
	private String markup;

	@Getter
	private final List<WebCmsComponentModel> members = new ArrayList<>();

	public ContainerWebCmsComponentModel( WebCmsComponentType containerType ) {
		super( containerType );
	}

	public ContainerWebCmsComponentModel( WebCmsComponent component, Collection<WebCmsComponentModel> components ) {
		this( component );
		members.addAll( components );
	}

	public ContainerWebCmsComponentModel( WebCmsComponent component ) {
		super( component );
		this.markup = component.getBody();
	}

	protected ContainerWebCmsComponentModel( WebCmsComponentModel template ) {
		super( template );
	}

	/**
	 * Add a member to the list.
	 *
	 * @param componentModel new member
	 */
	public void addMember( WebCmsComponentModel componentModel ) {
		members.add( componentModel );
	}

	/**
	 * @param name         of the component
	 * @param expectedType to coerce to
	 * @return member with the given name or null if not found
	 */
	public <U extends WebCmsComponentModel> U getMember( String name, Class<U> expectedType ) {
		Assert.notNull( expectedType );
		return expectedType.cast( getMember( name ) );
	}

	/**
	 * @param name of the component
	 * @return member with the given name or null if not found
	 */
	public WebCmsComponentModel getMember( String name ) {
		Assert.notNull( name );
		for ( WebCmsComponentModel member : members ) {
			if ( name.equals( member.getName() ) ) {
				return member;
			}
		}

		return null;
	}

	/**
	 * @return number of members in this container
	 */
	public int size() {
		return members.size();
	}

	/**
	 * @return true if container has no members
	 */
	public boolean isEmpty() {
		return members.isEmpty();
	}

	/**
	 * @return true if the container does not allow dynamic adding/sorting of members
	 */
	public boolean isFixed() {
		return TYPE_FIXED.equals( getComponentType().getAttribute( WebCmsComponentModel.TYPE_ATTRIBUTE ) );
	}

	/**
	 * You can always set markup on the container but the value of {@link #isMarkupSupported()} will determine if it will be used for rendering or not.
	 * This method simply indicates if markup has been set.
	 *
	 * @return true if markup is set on this container
	 */
	public boolean hasMarkup() {
		return StringUtils.isNotEmpty( markup );
	}

	/**
	 * @return true if the container supports markup for rendering - this is determined by the component type
	 * @see #SUPPORTS_MARKUP_ATTRIBUTE
	 */
	public boolean isMarkupSupported() {
		return StringUtils.equalsIgnoreCase( "true", getComponentType().getAttribute( SUPPORTS_MARKUP_ATTRIBUTE ) );
	}

	@Override
	public ContainerWebCmsComponentModel asComponentTemplate() {
		ContainerWebCmsComponentModel template = new ContainerWebCmsComponentModel( this );
		template.markup = markup;
		members.forEach( member -> template.addMember( member.asComponentTemplate() ) );
		return template;
	}
}
