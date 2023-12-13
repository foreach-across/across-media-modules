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

import com.foreach.across.modules.bootstrapui.elements.ButtonViewElement;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.bootstrapui.elements.builder.ColumnViewElementBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.processors.SaveEntityViewProcessor;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.EntityViewPageHelper;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewCommandValidator;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.links.EntityViewLinkBuilder;
import com.foreach.across.modules.entity.web.links.SingleEntityViewLinkBuilder;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentValidator;
import com.foreach.across.modules.webcms.domain.component.config.WebCmsObjectComponentViewsConfiguration;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import com.foreach.across.modules.webcms.web.WebCmsComponentAdminResources;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;

import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyles.css;
import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * Layouts the web component form pages, builds the actual component model and renders the form.
 * Mainly intended for indirect use by the {@link WebCmsObjectComponentViewsConfiguration}.
 * See the {@link WebCmsComponentsFormProcessor} if you want to add the ability to edit a certain
 * component to another form page.
 *
 * @author Arne Vandamme
 * @see WebCmsComponentsFormProcessor
 * @see WebCmsObjectComponentViewsConfiguration
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class SingleWebCmsComponentFormProcessor extends SaveEntityViewProcessor
{
	private final static String EXTENSION_NAME = "componentModel";

	private final EntityViewPageHelper entityViewPageHelper;
	private final WebCmsComponentModelService componentModelService;
	private final WebCmsComponentRepository componentRepository;
	private final WebCmsComponentModelAdminRenderService componentModelAdminRenderService;
	private final EntityViewCommandValidator entityViewCommandValidator;
	private final WebCmsComponentValidator componentValidator;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		dataBinder.setValidator( entityViewCommandValidator );

		super.initializeCommandObject( entityViewRequest, command, dataBinder );

		WebCmsComponentModel componentModel = componentModelService.buildModelForComponent( command.getEntity( WebCmsComponent.class ) );
		command.addExtension( EXTENSION_NAME, componentModel );
	}

	@Override
	protected void validateCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, Errors errors, HttpMethod httpMethod ) {
		WebCmsComponentModel componentModel = command.getExtension( EXTENSION_NAME, WebCmsComponentModel.class );

		if ( HttpMethod.POST.equals( httpMethod ) ) {
			boolean sharedValidation = !componentModel.hasOwner() || componentRepository.findOneByObjectId( componentModel.getOwnerObjectId() ) == null;
			Object[] validationHints = sharedValidation ? new Object[] { Default.class, WebCmsComponent.SharedComponentValidation.class } : new Object[0];
			entityViewRequest.getDataBinder().validate( validationHints );

			if ( !errors.hasErrors() ) {
				errors.pushNestedPath( "extensions[" + EXTENSION_NAME + "].component" );
				componentValidator.validate( componentModel.getComponent(), errors, validationHints );
				errors.popNestedPath();
			}
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
				(WebCmsComponentUpdateLinkBuilder) component -> entityViewRequest.getEntityViewContext().getLinkBuilder().forInstance( component ).updateView()
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

				String fromUrl = entityViewRequest.getWebRequest().getParameter( "from" );

				SingleEntityViewLinkBuilder redirectTarget = entityViewContext.getLinkBuilder().forInstance( savedEntity ).updateView();

				if ( !isNew && fromUrl != null ) {
					redirectTarget = redirectTarget.withFromUrl( fromUrl );
				}

				if ( entityViewRequest.hasPartialFragment() ) {
					redirectTarget = redirectTarget.withPartial( entityViewRequest.getPartialFragment() );
				}

				entityView.setRedirectUrl( redirectTarget.toUriString() );
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
			val ownerTrail = html.builders.ul( HtmlViewElement.Functions.css( "breadcrumb", "wcm-component-owner-trail" ) );
			if ( addToOwnerTrail( ownerTrail, componentModel.getObjectId(), entityViewRequest.getEntityViewContext().getLinkBuilder(), false ) ) {
				columnViewElementBuilder.add( ownerTrail );
			}
		}

		columnViewElementBuilder.add( componentModelAdminRenderService.createFormElement( componentModel, "extensions[" + EXTENSION_NAME + "]" ) );
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		if ( StringUtils.isEmpty( entityViewRequest.getWebRequest().getParameter( "from" ) ) ) {
			WebCmsComponentModel component = entityViewRequest.getCommand().getExtension( EXTENSION_NAME, WebCmsComponentModel.class );
			String ownerObjectId = component != null ? component.getOwnerObjectId() : null;
			ContainerViewElementUtils.find( container, "btn-cancel", ButtonViewElement.class )
			                         .ifPresent( btn -> {
				                         if ( StringUtils.isNotEmpty( ownerObjectId )
						                         && WebCmsUtils.isObjectIdForCollection( ownerObjectId, WebCmsComponent.COLLECTION_ID ) ) {
					                         EntityViewContext entityViewContext = entityViewRequest.getEntityViewContext();
					                         WebCmsComponent owner = componentRepository.findOneByObjectId( ownerObjectId ).orElse( null );
					                         btn.setUrl( entityViewContext.getLinkBuilder().forInstance( owner ).updateView().toUriString() );
				                         }
			                         } );
		}
	}

	private boolean addToOwnerTrail( NodeViewElementBuilder breadcrumb, String objectId, EntityViewLinkBuilder linkBuilder, boolean createLink ) {
		WebCmsComponent owner = componentRepository.findOneByObjectId( objectId ).orElse( null );
		if ( owner != null ) {
			String title = StringUtils.defaultIfBlank( owner.getTitle(), StringUtils.defaultIfBlank( owner.getName(), owner.getComponentType().getName() ) );
			breadcrumb.addFirst(
					html.builders
							.li()
							.attribute( "title", owner.getName() )
							.with( css.breadcrumb.item )
							.add(
									createLink
											? bootstrap.builders.link()
											                    .url( linkBuilder.forInstance( owner ).updateView().toUriString() )
											                    .text( title )
											: html.builders.text( title )
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
		webResourceRegistry.addPackage( WebCmsComponentAdminResources.NAME );
	}
}
