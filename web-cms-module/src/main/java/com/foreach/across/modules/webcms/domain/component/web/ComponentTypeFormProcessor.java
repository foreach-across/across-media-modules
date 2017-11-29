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

import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.support.ValueFetcher;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
@RequiredArgsConstructor
//@Component
public class ComponentTypeFormProcessor extends EntityViewProcessorAdapter implements ValueFetcher<WebCmsObject>
{
	public static final String COMPONENT_TYPES_CONTROL_NAME = "extensions[componentTypes].allowed";

	private final EntityViewRequest entityViewRequest;
	private final WebCmsTypeSpecifierLinkRepository typeLinkRepository;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		command.addExtension( "componentTypes", new componentTypesHolder() );
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() ) {
			WebCmsObject owner = command.getEntity( WebCmsObject.class );
			componentTypesHolder typesHolder = command.getExtension( "componentTypes", componentTypesHolder.class );

			Map<WebCmsTypeSpecifier, WebCmsTypeSpecifierLink> currentLinks
					= typeLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( owner.getObjectId(), WebCmsComponentType.OBJECT_TYPE )
					                    .stream()
					                    .collect( Collectors.toMap( WebCmsTypeSpecifierLink::getTypeSpecifier, Function.identity() ) );

			List<WebCmsTypeSpecifierLink> linksToCreate = new ArrayList<>();
			typesHolder.getAllowed().forEach( componentType -> {
				if ( currentLinks.remove( componentType ) == null ) {
					WebCmsTypeSpecifierLink link = new WebCmsTypeSpecifierLink();
					link.setOwner( owner );
					link.setTypeSpecifier( componentType );
					link.setLinkType( WebCmsComponentType.OBJECT_TYPE );

					linksToCreate.add( link );
				}
			} );

			currentLinks.values().forEach( typeLinkRepository::delete );
			linksToCreate.forEach( typeLinkRepository::save );
		}
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		ContainerViewElementUtils.move( container, "formGroup-componentTypes", SingleEntityFormViewProcessor.RIGHT_COLUMN );
	}

	@Override
	public Object getValue( WebCmsObject entity ) {
		if ( entityViewRequest.getHttpMethod() == HttpMethod.POST && entityViewRequest.getCommand().hasExtension( "componentTypes" ) ) {
			return entityViewRequest.getCommand().getExtension( "componentTypes", componentTypesHolder.class ).allowed;
		}

		return entity.isNew()
				? Collections.emptySet()
				: typeLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( entity.getObjectId(), WebCmsComponentType.OBJECT_TYPE )
				                    .stream()
				                    .map( WebCmsTypeSpecifierLink::getTypeSpecifier )
				                    .collect( Collectors.toSet() );
	}

	@Data
	class componentTypesHolder
	{
		Set<WebCmsComponentType> allowed = new HashSet<>();
	}
}
