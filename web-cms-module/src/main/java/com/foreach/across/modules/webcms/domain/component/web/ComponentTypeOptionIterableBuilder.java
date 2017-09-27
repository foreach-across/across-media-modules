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

package com.foreach.across.modules.webcms.domain.component.web;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.entity.views.bootstrapui.options.OptionIterableBuilder;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsAllowedComponentTypeFetcher;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Builds the {@link WebCmsComponentType} options for a {@link WebCmsObject}.
 *
 * @see WebCmsAllowedComponentTypeFetcher
 * @author Steven Gentens
 * @since 0.0.3
 */
@RequiredArgsConstructor
@Component
public class ComponentTypeOptionIterableBuilder implements OptionIterableBuilder
{
	private final WebCmsComponentRepository componentRepository;
	private final WebCmsMultiDomainService multiDomainService;
	private Collection<WebCmsAllowedComponentTypeFetcher> componentTypeFetchers = Collections.emptyList();

	@Override
	public Iterable<OptionFormElementBuilder> buildOptions( ViewElementBuilderContext builderContext ) {
		EntityViewCommand command = builderContext.getAttribute( EntityViewCommand.class );
		Object entity = command.getEntity();
		String ownerObjectId = null;
		if ( entity != null ) {
			ownerObjectId = ( (WebCmsComponent) entity ).getOwnerObjectId();
		}

		WebCmsObject owner = componentRepository.findOneByObjectId( ownerObjectId );

		Collection<WebCmsComponentType> allowedTypes = getAllowedTypes( owner, multiDomainService.getCurrentDomain() );

		return allowedTypes.stream()
		                   .map( type -> new OptionFormElementBuilder().rawValue( type )
		                                                               .value( type.getId() )
		                                                               .controlName( "entity.componentType" )
		                                                               .text( type.getName() )
		                   ).collect( Collectors.toList() );
	}

	@SuppressWarnings("unchecked")
	private Collection<WebCmsComponentType> getAllowedTypes( WebCmsObject owner, WebCmsDomain currentDomain ) {
		return componentTypeFetchers.stream()
		                            .filter( fetcher -> fetcher.supports( owner, currentDomain ) )
		                            .findFirst()
		                            .orElseThrow( () -> new IllegalArgumentException( "Unable to find a handler for " + owner ) )
		                            .fetchComponentTypes( owner, currentDomain );
	}

	@Autowired
	void setComponentTypeFetchers( @RefreshableCollection(includeModuleInternals = true, incremental = true) Collection<WebCmsAllowedComponentTypeFetcher> componentTypeFetchers ) {
		this.componentTypeFetchers = componentTypeFetchers;
	}
}
