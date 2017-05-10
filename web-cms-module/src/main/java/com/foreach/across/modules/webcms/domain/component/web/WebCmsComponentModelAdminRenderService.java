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
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.FormGroupElement;
import com.foreach.across.modules.bootstrapui.elements.FormInputElement;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.bootstrapui.elements.processor.ControlNamePrefixingPostProcessor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderHelper;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.helpers.EntityViewElementBatch;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Central API for building the administration UI for editing components.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Service
@RequiredArgsConstructor
public final class WebCmsComponentModelAdminRenderService
{
	private final BootstrapUiFactory bootstrapUiFactory;
	private final EntityViewElementBuilderHelper builderHelper;
	private final WebCmsComponentRepository componentRepository;

	private Collection<WebCmsComponentModelContentAdminRenderer> contentRenderers = Collections.emptyList();
	private Collection<WebCmsComponentModelMetadataAdminRenderer> metadataRenderers = Collections.emptyList();
	private Collection<WebCmsComponentModelMembersAdminRenderer> membersRenderers = Collections.emptyList();

	public WebCmsComponentModelFormElementBuilder createFormElement( WebCmsComponentModel componentModel, String controlNamePrefix ) {
		WebCmsComponentModelFormElementBuilder formElementBuilder = new WebCmsComponentModelFormElementBuilder( componentModel );
		formElementBuilder.settings( createSettingsViewElementBuilder( componentModel, controlNamePrefix ) );

		createContentViewElementBuilder( componentModel, controlNamePrefix ).ifPresent( formElementBuilder::content );
		createMembersViewElementBuilder( componentModel, controlNamePrefix ).ifPresent( formElementBuilder::members );
		createMetadataViewElementBuilder( componentModel, controlNamePrefix ).ifPresent( formElementBuilder::metadata );

		// if we're dealing with an extensible container - determine if sort and or add options should be shown
		if ( componentModel instanceof ContainerWebCmsComponentModel && !( (ContainerWebCmsComponentModel) componentModel ).isFixed() ) {
			formElementBuilder.showAddComponentButton( true );
			formElementBuilder.sortableContainer( true );
		}

		formElementBuilder.add(
				bootstrapUiFactory.hidden()
				                  .controlName( controlNamePrefix + ".component.sortIndex" )
				                  .value( componentModel.getComponent().getSortIndex() )
		);

		return formElementBuilder;
	}

	public ViewElementBuilder createSettingsViewElementBuilder( WebCmsComponentModel componentModel, String controlNamePrefix ) {
		Map<String, Object> builderHints = new HashMap<>();
		builderHints.put( "componentType", ViewElementMode.FORM_READ );
		builderHints.put( "lastModified", ViewElementMode.FORM_READ );

		EntityViewElementBatch<WebCmsComponent> generalSettingsBuilder = builderHelper.createBatchForEntityType( WebCmsComponent.class );
		generalSettingsBuilder.setPropertySelector( EntityPropertySelector.of( "componentType", "title", "name", "lastModified" ) );
		generalSettingsBuilder.setViewElementMode( ViewElementMode.FORM_WRITE );
		generalSettingsBuilder.setBuilderHints( builderHints );
		generalSettingsBuilder.setEntity( componentModel.getComponent() );

		val messageCodeResolver = generalSettingsBuilder.getAttribute( EntityMessageCodeResolver.class );
		generalSettingsBuilder.setAttribute(
				EntityMessageCodeResolver.class,
				messageCodeResolver.prefixedResolver( componentModel.hasOwner() ? "views[updateMember]" : "views[updateView]" )
		);

		val formGroups = generalSettingsBuilder.build();

		WebCmsComponent ownerContainer = componentModel.hasOwner() ? componentRepository.findOneByObjectId( componentModel.getOwnerObjectId() ) : null;

		return bootstrapUiFactory.row()
		                         .add(
				                         bootstrapUiFactory.column( Grid.Device.MEDIUM.width( 6 ) )
				                                           .add( formGroups.get( "title" ) )
				                                           .add( formGroups.get( "name" ) )
				                                           .add( formGroups.get( "sortIndex" ) )
		                         )
		                         .add(
				                         bootstrapUiFactory.column( Grid.Device.MEDIUM.width( 6 ) )
				                                           .add( formGroups.get( "componentType" ) )
				                                           .add( formGroups.get( "lastModified" ) )
		                         )
		                         .postProcessor( ( builderContext, container ) -> {
			                         ControlNamePrefixingPostProcessor controlNamePrefixingPostProcessor = new ControlNamePrefixingPostProcessor(
					                         controlNamePrefix + ".component" );
			                         container.findAll( FormInputElement.class )
			                                  .forEach( e -> controlNamePrefixingPostProcessor.postProcess( builderContext, e ) );
		                         } )
		                         .postProcessor( ( builderContext, container ) -> {
			                         if ( ownerContainer != null ) {
				                         container.find( "formGroup-title", FormGroupElement.class )
				                                  .ifPresent( group -> group.setRequired( false ) );
				                         container.find( "formGroup-name", FormGroupElement.class )
				                                  .ifPresent( group -> group.setRequired( false ) );
			                         }
		                         } );
	}

	@SuppressWarnings("unchecked")
	public Optional<ViewElementBuilder> createContentViewElementBuilder( WebCmsComponentModel componentModel, String controlNamePrefix ) {
		return contentRenderers.stream()
		                       .filter( r -> r.supports( componentModel ) )
		                       .findFirst()
		                       .map( r -> r.createContentViewElementBuilder( componentModel, controlNamePrefix ) );
	}

	@SuppressWarnings("unchecked")
	public Optional<ViewElementBuilder> createMembersViewElementBuilder( WebCmsComponentModel componentModel, String controlNamePrefix ) {
		return membersRenderers.stream()
		                       .filter( r -> r.supports( componentModel ) )
		                       .findFirst()
		                       .map( r -> r.createMembersViewElementBuilder( componentModel, controlNamePrefix ) );
	}

	@SuppressWarnings("unchecked")
	public Optional<ViewElementBuilder> createMetadataViewElementBuilder( WebCmsComponentModel componentModel, String controlNamePrefix ) {
		if ( componentModel.hasMetadata() ) {
			return metadataRenderers.stream()
			                        .filter( r -> r.supports( componentModel, componentModel.getMetadata() ) )
			                        .findFirst()
			                        .map( r -> r.createMetadataViewElementBuilder( componentModel, componentModel.getMetadata(), controlNamePrefix ) );
		}

		return Optional.empty();
	}

	@Autowired
	void setContentRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsComponentModelContentAdminRenderer> contentRenderers ) {
		this.contentRenderers = contentRenderers;
	}

	@Autowired
	void setMetadataRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsComponentModelMetadataAdminRenderer> metadataRenderers ) {
		this.metadataRenderers = metadataRenderers;
	}

	@Autowired
	void setMembersRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsComponentModelMembersAdminRenderer> membersRenderers ) {
		this.membersRenderers = membersRenderers;
	}
}
