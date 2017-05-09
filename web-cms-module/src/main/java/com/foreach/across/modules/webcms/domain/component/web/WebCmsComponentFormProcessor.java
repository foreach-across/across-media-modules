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

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.bootstrapui.elements.builder.ColumnViewElementBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.processors.SaveEntityViewProcessor;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.EntityViewPageHelper;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentValidator;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.web.TextWebComponentResources;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

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
	private final WebCmsComponentModelService componentModelService;
	private final WebCmsComponentRepository componentRepository;
	private final WebCmsComponentModelAdminRenderService componentModelAdminRenderService;
	private final WebCmsComponentValidator componentValidator;
	private final BootstrapUiFactory bootstrapUiFactory;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		super.initializeCommandObject( entityViewRequest, command, dataBinder );

		WebCmsComponentModel componentModel = componentModelService.buildModelForComponent( command.getEntity( WebCmsComponent.class ) );
		command.addExtension( EXTENSION_NAME, componentModel );
	}

	@Override
	protected void validateCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, Errors errors, HttpMethod httpMethod ) {
		if ( HttpMethod.POST.equals( httpMethod ) && !errors.hasErrors() ) {
			WebCmsComponentModel componentModel = command.getExtension( EXTENSION_NAME, WebCmsComponentModel.class );

			errors.pushNestedPath( "extensions[" + EXTENSION_NAME + "].component" );
			componentValidator.validate( componentModel.getComponent(), errors );
			errors.popNestedPath();
		}
	}

	@Override
	protected void doControl( EntityViewRequest entityViewRequest,
	                          EntityView entityView,
	                          EntityViewCommand command,
	                          BindingResult bindingResult,
	                          HttpMethod httpMethod ) {
		UrlPathHelper pathHelper = new UrlPathHelper();
		entityView.addAttribute(
				"currentComponentUrl",
				pathHelper.getPathWithinApplication( entityViewRequest.getWebRequest().getNativeRequest( HttpServletRequest.class ) )
		);
		entityView.addAttribute(
				"componentLinkBuilder",
				entityViewRequest.getEntityViewContext().getLinkBuilder()
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !hasComponentModelErrors( bindingResult ) ) {
			try {
				EntityViewContext entityViewContext = entityViewRequest.getEntityViewContext();

				WebCmsComponentModel componentModel = command.getExtension( EXTENSION_NAME, WebCmsComponentModel.class );
				boolean isNew = componentModel.isNew();

				WebCmsComponent savedEntity = componentModelService.save( componentModel );

				entityViewPageHelper.addGlobalFeedbackAfterRedirect( entityViewRequest, Style.SUCCESS,
				                                                     isNew ? "feedback.entityCreated" : "feedback.entityUpdated" );

				String redirectTargetUrl = entityViewRequest.getWebRequest().getParameter( "from" );

				if ( redirectTargetUrl == null ) {
					redirectTargetUrl = entityViewContext.getLinkBuilder().update( savedEntity );
				}

				if ( entityViewRequest.hasPartialFragment() ) {
					entityView.setRedirectUrl(
							UriComponentsBuilder.fromUriString( redirectTargetUrl )
							                    .queryParam( WebTemplateInterceptor.PARTIAL_PARAMETER, entityViewRequest.getPartialFragment() )
							                    .toUriString()
					);
				}
				else {
					entityView.setRedirectUrl( redirectTargetUrl );
				}

			}
			catch ( RuntimeException e ) {
				entityViewPageHelper.throwOrAddExceptionFeedback( entityViewRequest, "feedback.entitySaveFailed", e );
			}
		}
	}

	private boolean hasComponentModelErrors( Errors errors ) {
		return errors.hasGlobalErrors() || errors.getFieldErrors().stream().anyMatch( field -> !field.getField().startsWith( "entity." ) );
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       ViewElementBuilderContext builderContext ) {
		WebCmsComponentModel componentModel = entityViewRequest.getCommand().getExtension( EXTENSION_NAME, WebCmsComponentModel.class );

		ColumnViewElementBuilder columnViewElementBuilder = builderMap.get( SingleEntityFormViewProcessor.LEFT_COLUMN, ColumnViewElementBuilder.class );

		if ( componentModel.hasOwner() ) {
			val ownerTrail = bootstrapUiFactory.node( "ul" ).css( "breadcrumb", "wcm-component-owner-trail" );
			if ( addToOwnerTrail( ownerTrail, componentModel.getObjectId(), entityViewRequest.getEntityViewContext().getLinkBuilder(), false ) ) {
				columnViewElementBuilder.add( ownerTrail );
			}
		}

		columnViewElementBuilder.add( componentModelAdminRenderService.createFormElement( componentModel, "extensions[" + EXTENSION_NAME + "]" ) );
	}

	private boolean addToOwnerTrail( NodeViewElementBuilder breadcrumb, String objectId, EntityLinkBuilder linkBuilder, boolean createLink ) {
		WebCmsComponent owner = componentRepository.findOneByObjectId( objectId );
		if ( owner != null ) {
			String title = StringUtils.isEmpty( owner.getTitle() ) ? owner.getComponentType().getName() : owner.getTitle();
			breadcrumb.addFirst(
					bootstrapUiFactory.node( "li" )
					                  .attribute( "title", owner.getName() )
					                  .add(
							                  createLink
									                  ? bootstrapUiFactory.link().url( linkBuilder.update( owner ) ).text( title )
									                  : bootstrapUiFactory.text( title )
					                  )
			);

			if ( owner.hasOwner() ) {
				return addToOwnerTrail( breadcrumb, owner.getOwnerObjectId(), linkBuilder, true ) || createLink;
			}

			return createLink;
		}

		return false;
	}

	@Override
	protected void registerWebResources( EntityViewRequest entityViewRequest, EntityView entityView, WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addPackage( TextWebComponentResources.NAME );
	}
}
