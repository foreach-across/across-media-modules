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

import com.foreach.across.modules.bootstrapui.elements.FormGroupElement;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

/**
 * Applies some UI changes when modifying a member of a container.
 * Also set the default sort index for the new member.
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class ContainerMemberViewProcessor extends EntityViewProcessorAdapter
{
	private final WebCmsComponentModelService componentModelService;

	@Override
	protected void validateCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, Errors errors, HttpMethod httpMethod ) {
		if ( HttpMethod.POST.equals( entityViewRequest.getHttpMethod() ) ) {
			val component = command.getEntity( WebCmsComponent.class );
			if ( component != null && component.hasOwner() ) {
				val ownerModel = componentModelService.getComponentModel( component.getOwnerObjectId() );
				if ( ownerModel != null && ownerModel instanceof ContainerWebCmsComponentModel ) {
					component.setSortIndex( ( (ContainerWebCmsComponentModel) ownerModel ).size() + 1 );
				}
			}
		}
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       EntityViewCommand command,
	                       BindingResult bindingResult ) {
		if ( entityView.isRedirect() ) {
			String redirectTargetUrl = entityViewRequest.getWebRequest().getParameter( "from" );

			if ( redirectTargetUrl != null ) {
				entityView.setRedirectUrl( redirectTargetUrl );
			}
		}
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		WebCmsComponent component = entityViewRequest.getCommand().getEntity( WebCmsComponent.class );

		if ( component == null ) {
			component = entityViewRequest.getEntityViewContext().getEntity( WebCmsComponent.class );
		}

		if ( component != null && component.hasOwner() ) {
			entityViewRequest.getPageContentStructure().withNav( ContainerViewElement::clearChildren );
		}

		container.find( "formGroup-title", FormGroupElement.class )
		         .ifPresent( group -> group.setRequired( false ) );
		container.find( "formGroup-name", FormGroupElement.class )
		         .ifPresent( group -> group.setRequired( false ) );
	}
}
