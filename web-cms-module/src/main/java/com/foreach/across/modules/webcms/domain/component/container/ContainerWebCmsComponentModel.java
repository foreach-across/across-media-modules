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
import com.foreach.across.modules.webcms.domain.component.model.OrderedWebComponentModelSet;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of other {@link WebCmsComponentModel}s.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class ContainerWebCmsComponentModel extends WebCmsComponentModel
{
	@Getter
	private final List<WebCmsComponentModel> members = new ArrayList<>();

	public ContainerWebCmsComponentModel() {
	}

	public ContainerWebCmsComponentModel( WebCmsComponent component ) {
		super( component );
	}

	public ContainerWebCmsComponentModel( WebCmsComponent component, OrderedWebComponentModelSet components ) {
		super( component );
		members.addAll( components.getOrdered() );
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
	 * @return true if container has no members
	 */
	public boolean isEmpty() {
		return members.isEmpty();
	}

	@Override
	public ContainerWebCmsComponentModel asTemplate() {
		ContainerWebCmsComponentModel template = new ContainerWebCmsComponentModel( getComponent().asTemplate() );
		members.forEach( member -> template.addMember( member.asTemplate() ) );
		return template;
	}
}
