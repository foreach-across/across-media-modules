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

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.bootstrapui.elements.builder.ColumnViewElementBuilder;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderHelper;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.processors.SaveEntityViewProcessor;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.EntityViewPageHelper;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.web.TextWebComponentResources;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Layouts the web component form pages, builds the actual component model and renders the form.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class WebCmsComponentFormProcessor extends SaveEntityViewProcessor
{
	private final static String EXTENSION_NAME = "componentModel";

	private final EntityViewPageHelper entityViewPageHelper;

	private final EntityViewElementBuilderHelper builderHelper;
	private final WebCmsComponentModelService componentModelService;
	private final WebCmsComponentModelAdminRenderService componentModelAdminRenderService;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		super.initializeCommandObject( entityViewRequest, command, dataBinder );

		WebCmsComponentModel componentModel = componentModelService.buildModelForComponent( command.getEntity( WebCmsComponent.class ) );
		command.addExtension( EXTENSION_NAME, componentModel );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() ) {
			try {
				EntityViewContext entityViewContext = entityViewRequest.getEntityViewContext();

				WebCmsComponentModel componentModel = command.getExtension( EXTENSION_NAME, WebCmsComponentModel.class );
				componentModel.setComponent( command.getEntity( WebCmsComponent.class ) );

				boolean isNew = componentModel.isNew();

				WebCmsComponent savedEntity = componentModelService.save( componentModel );

				entityViewPageHelper.addGlobalFeedbackAfterRedirect( entityViewRequest, Style.SUCCESS,
				                                                     isNew ? "feedback.entityCreated" : "feedback.entityUpdated" );

				if ( entityViewRequest.hasPartialFragment() ) {
					entityView.setRedirectUrl(
							UriComponentsBuilder.fromUriString( entityViewContext.getLinkBuilder().update( savedEntity ) )
							                    .queryParam( WebTemplateInterceptor.PARTIAL_PARAMETER, entityViewRequest.getPartialFragment() )
							                    .toUriString()
					);
				}
				else {
					entityView.setRedirectUrl( entityViewContext.getLinkBuilder().update( savedEntity ) );
				}

			}
			catch ( RuntimeException e ) {
				entityViewPageHelper.throwOrAddExceptionFeedback( entityViewRequest, "feedback.entitySaveFailed", e );
			}
		}
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       ViewElementBuilderContext builderContext ) {
		WebCmsComponentModel componentModel = entityViewRequest.getCommand().getExtension( EXTENSION_NAME, WebCmsComponentModel.class );

		builderMap.get( SingleEntityFormViewProcessor.LEFT_COLUMN, ColumnViewElementBuilder.class )
		          .add( componentModelAdminRenderService.createFormElement( componentModel, "extensions[" + EXTENSION_NAME + "]" ) );
		/*
		WebCmsComponentModel componentModel = entityViewRequest.getCommand().getExtension( EXTENSION_NAME, WebCmsComponentModel.class );

		Map<String, Object> builderHints = new HashMap<>();
		builderHints.put( "componentType", ViewElementMode.FORM_READ );
		builderHints.put( "lastModified", ViewElementMode.FORM_READ );

		EntityViewElementBatch<WebCmsComponent> generalSettingsBuilder = builderHelper.createBatchForEntityType( WebCmsComponent.class );
		generalSettingsBuilder.setPropertySelector( EntityPropertySelector.of( "componentType", "title", "name", "sortIndex", "lastModified" ) );
		generalSettingsBuilder.setViewElementMode( ViewElementMode.FORM_WRITE );
		generalSettingsBuilder.setBuilderHints( builderHints );
		generalSettingsBuilder.setEntity( EntityViewElementUtils.currentEntity( builderContext, WebCmsComponent.class ) );

		ContainerViewElement settings = new ContainerViewElement( "settings" );
		generalSettingsBuilder.build().forEach( ( name, element ) -> settings.addChild( element ) );

		String prefix = "extensions[" + EXTENSION_NAME + "]";

		ControlNamePrefixingPostProcessor controlNamePrefixingPostProcessor = new ControlNamePrefixingPostProcessor( prefix + ".component" );
		settings.findAll( FormInputElement.class )
		        .forEach( e -> controlNamePrefixingPostProcessor.postProcess( builderContext, e ) );

		ContainerViewElementBuilder contentBuilder = new ContainerViewElementBuilder().name( "content" );
		contentBuilder.add( componentModelAdminRenderService.createContentViewElementBuilder( componentModel, prefix ) );

		ColumnViewElementBuilder columnViewElementBuilder = builderMap.get( SingleEntityFormViewProcessor.LEFT_COLUMN, ColumnViewElementBuilder.class );
		columnViewElementBuilder
				.htmlId( prefix )
				.attribute( "componentModel", componentModel )
				.customTemplate( "th/adminWebCms/playground :: body" )
				.add( settings )
				.add( contentBuilder );

		componentModelAdminRenderService.createMetadataViewElementBuilder( componentModel, prefix )
		                                .ifPresent( metadata -> {
			                                ContainerViewElementBuilder metadataContainer = new ContainerViewElementBuilder()
					                                .name( "metadata" )
					                                .add( metadata );
			                                columnViewElementBuilder.add( metadataContainer );
		                                } );
		                                */
	}

	@Override
	protected void registerWebResources( EntityViewRequest entityViewRequest, EntityView entityView, WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addPackage( TextWebComponentResources.NAME );
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		//container.removeAllFromTree( "formGroup-componentType", "formGroup-title", "formGroup-name", "formGroup-sortIndex", "formGroup-lastModified" );
		/*
		ContainerViewElementUtils.move( container, "formGroup-componentType", SingleEntityFormViewProcessor.RIGHT_COLUMN );
		ContainerViewElementUtils.move( container, "formGroup-title", SingleEntityFormViewProcessor.RIGHT_COLUMN );
		ContainerViewElementUtils.move( container, "formGroup-name", SingleEntityFormViewProcessor.RIGHT_COLUMN );
		ContainerViewElementUtils.move( container, "formGroup-sortIndex", SingleEntityFormViewProcessor.RIGHT_COLUMN );
		ContainerViewElementUtils.move( container, "formGroup-lastModified", SingleEntityFormViewProcessor.RIGHT_COLUMN );
		*/
	}
}
