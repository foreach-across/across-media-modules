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

package com.foreach.across.modules.webcms.domain.url.web;

import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.support.EntityViewPageHelper;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElements;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.support.EndpointModificationType;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;

import java.util.Collections;
import java.util.Optional;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor.LEFT_COLUMN;

/**
 * Renders a form to force the user to select an action to be taken with regards to the primary URL of an asset.
 * The user will be automatically redirected to this form if the automatic update of the primary URL failed.
 *
 * @author Arne Vandamme
 * @see WebCmsAssetPrimaryUrlFailureDetectionProcessor
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class WebCmsAssetPrimaryUrlFailedFormProcessor extends EntityViewProcessorAdapter
{
	private final WebCmsEndpointService endpointService;
	private final EntityViewPageHelper pageHelper;
	private final WebCmsUrlRepository urlRepository;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		WebCmsAsset<?> asset = entityViewRequest.getEntityViewContext().getEntity( WebCmsAsset.class );
		Optional<WebCmsUrl> primaryUrl = endpointService.getPrimaryUrlForAsset( asset );

		PrimaryUrlUpdateFormData data = new PrimaryUrlUpdateFormData();
		data.setNewPrimaryUrl( entityViewRequest.getWebRequest().getParameter( "requestedUrl" ) );
		data.setCurrentPrimaryUrl( primaryUrl.orElse( null ) );
		data.setAction( primaryUrl.isPresent() ? PrimaryUrlUpdateAction.LOCK_CURRENT_PRIMARY_URL : PrimaryUrlUpdateAction.CREATE_NEW_PRIMARY_URL );

		command.setEntity( data );
	}

	@Override
	protected void preProcess( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command ) {
		PrimaryUrlUpdateFormData data = command.getEntity( PrimaryUrlUpdateFormData.class );

		if ( data.getCurrentPrimaryUrl() != null && data.getCurrentPrimaryUrl().isPrimaryLocked() ) {
			// Edge case: if primary url is already locked, send back to update page as there is no longer an issue
			entityView.setRedirectUrl(
					entityViewRequest.getEntityViewContext().getLinkBuilder()
					                 .forInstance( entityViewRequest.getEntityViewContext().getEntity() )
					                 .updateView()
					                 .toUriString()
			);
		}
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		PrimaryUrlUpdateFormData data = command.getEntity( PrimaryUrlUpdateFormData.class );
		WebCmsAsset<?> asset = entityViewRequest.getEntityViewContext().getEntity( WebCmsAsset.class );

		boolean actionCompleted = true;

		if ( data.getAction() == PrimaryUrlUpdateAction.LOCK_CURRENT_PRIMARY_URL ) {
			actionCompleted = lockCurrentPrimaryUrl( entityViewRequest, asset );
		}
		else if ( data.getAction() == PrimaryUrlUpdateAction.CREATE_NEW_PRIMARY_URL ) {
			actionCompleted = createNewPrimaryUrl( entityViewRequest, asset, data.getNewPrimaryUrl() );
		}

		if ( actionCompleted ) {
			entityView.setRedirectUrl( entityViewRequest.getEntityViewContext().getLinkBuilder().forInstance( asset ).updateView().toUriString() );
		}
	}

	private boolean createNewPrimaryUrl( EntityViewRequest entityViewRequest, WebCmsAsset<?> asset, String newPrimaryUrl ) {
		BindingResult errors = entityViewRequest.getBindingResult();

		if ( StringUtils.isBlank( newPrimaryUrl ) ) {
			errors.rejectValue( "entity.newPrimaryUrl", "NotBlank" );
		}
		else {
			ModificationReport<EndpointModificationType, WebCmsUrl> modificationReport
					= endpointService.updateOrCreatePrimaryUrlForAsset( newPrimaryUrl, asset, false );
			if ( modificationReport.getModificationStatus() == ModificationStatus.FAILED ) {
				errors.rejectValue( "entity.newPrimaryUrl", "alreadyExists" );
			}
			else {
				WebCmsUrl primaryUrl = modificationReport.getNewValue().toDto();
				primaryUrl.setPrimaryLocked( true );
				urlRepository.save( primaryUrl );
				pageHelper.addGlobalFeedbackAfterRedirect( entityViewRequest, Style.SUCCESS, "forms.primaryUrlFailed.feedback.primaryUrlCreated" );
			}
		}

		return !errors.hasErrors();
	}

	private boolean lockCurrentPrimaryUrl( EntityViewRequest entityViewRequest, WebCmsAsset<?> asset ) {
		endpointService
				.getPrimaryUrlForAsset( asset )
				.ifPresent( url -> {
					WebCmsUrl dto = url.toDto();
					dto.setPrimaryLocked( true );
					urlRepository.save( dto );
					pageHelper.addGlobalFeedbackAfterRedirect( entityViewRequest, Style.SUCCESS, "forms.primaryUrlFailed.feedback.primaryUrlLocked" );
				} );
		return true;
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       ViewElementBuilderContext builderContext ) {
		PrimaryUrlUpdateFormData data = entityViewRequest.getCommand().getEntity( PrimaryUrlUpdateFormData.class );
		EntityMessageCodeResolver codeResolver = entityViewRequest.getEntityViewContext().getMessageCodeResolver();

		val form = HtmlViewElements.html.builders.container();

		form.add(
				bootstrap.builders
						.alert()
						.warning()
						.add(
								HtmlViewElements.html.builders.unescapedText(
										codeResolver.getMessageWithFallback( "description", null )
								)
						)
		);

		if ( data.getCurrentPrimaryUrl() != null ) {
			form.add(
					bootstrap.builders
							.formGroup()
							.control(
									bootstrap.builders
											.radio()
											.controlName( "entity.action" )
											.value( PrimaryUrlUpdateAction.LOCK_CURRENT_PRIMARY_URL.name() )
											.selected( data.getAction() == PrimaryUrlUpdateAction.LOCK_CURRENT_PRIMARY_URL )
											.label(
													codeResolver
															.getMessageWithFallback( "action.lockPrimaryUrl", new Object[] { data.getCurrentPrimaryUrlValue() },
															                         null )
											)
							)
							.helpBlock( codeResolver.getMessageWithFallback( "action.lockPrimaryUrl[description]", null ) )
			);
		}

		form.add(
				bootstrap.builders
						.radio()
						.controlName( "entity.action" )
						.value( PrimaryUrlUpdateAction.CREATE_NEW_PRIMARY_URL.name() )
						.selected( data.getAction() == PrimaryUrlUpdateAction.CREATE_NEW_PRIMARY_URL )
						.label( codeResolver.getMessageWithFallback( "action.createPrimaryUrl", null ) )
		);

		form.add(
				bootstrap.builders
						.formGroup()
						.control(
								bootstrap.builders
										.textbox()
										.controlName( "entity.newPrimaryUrl" )
										.placeholder( codeResolver.getMessageWithFallback( "action.createPrimaryUrl[placeholder]", null ) )
										.text( data.getNewPrimaryUrl() )
										.attribute(
												"data-dependson",
												Collections.singletonMap(
														"[name='entity.action']",
														Collections.singletonMap( "values", Collections
																.singleton( PrimaryUrlUpdateAction.CREATE_NEW_PRIMARY_URL.name() ) )
												)
										)
						)
						.descriptionBlock( codeResolver.getMessageWithFallback( "action.createPrimaryUrl[description]", null ) )
		);

		form.add(
				bootstrap.builders
						.formGroup()
						.control(
								bootstrap.builders
										.radio()
										.controlName( "entity.action" )
										.value( PrimaryUrlUpdateAction.DO_NOTHING.name() )
										.selected( data.getAction() == PrimaryUrlUpdateAction.DO_NOTHING )
										.label( codeResolver.getMessageWithFallback( "action.doNothing", null ) )
						)
						.helpBlock( codeResolver.getMessageWithFallback( "action.doNothing[help]", null ) )
		);

		builderMap.get( LEFT_COLUMN, ContainerViewElementBuilderSupport.class ).add( form );
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		container.removeFromTree( "btn-cancel" );
	}

	enum PrimaryUrlUpdateAction
	{
		LOCK_CURRENT_PRIMARY_URL,
		CREATE_NEW_PRIMARY_URL,
		DO_NOTHING
	}

	@Data
	static class PrimaryUrlUpdateFormData
	{
		PrimaryUrlUpdateAction action = PrimaryUrlUpdateAction.LOCK_CURRENT_PRIMARY_URL;
		String newPrimaryUrl;
		WebCmsUrl currentPrimaryUrl;

		String getCurrentPrimaryUrlValue() {
			return currentPrimaryUrl != null ? currentPrimaryUrl.getPath() : null;
		}
	}
}
