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

import com.foreach.across.core.annotations.Event;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.endpoint.support.PrimaryUrlForAssetFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Checks if primary URL update failed for the asset being updated.
 * Since failure is triggered by an event, it is possible we receive updates from other requests.
 * To trace back the original call, we register the asset instance on the request attributes and
 * we only register an event if it is for exactly the same asset instance reference.
 * <p/>
 * If primary URL update failed, will redirect the user to the <em>primaryUrlFailed</em> view of the asset.
 *
 * @author Arne Vandamme
 * @see WebCmsAssetPrimaryUrlFailedFormProcessor
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@Slf4j
public final class WebCmsAssetPrimaryUrlFailureDetectionProcessor extends EntityViewProcessorAdapter
{
	@Event
	void detectPrimaryUrlUpdateFailure( PrimaryUrlForAssetFailedEvent event ) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

		if ( requestAttributes != null ) {
			// we can receive events triggered by other threads
			WebCmsAsset assetToListenFor = (WebCmsAsset) requestAttributes.getAttribute( getClass().getName(), RequestAttributes.SCOPE_REQUEST );
			if ( assetToListenFor != null && assetToListenFor == event.getAsset() ) {
				LOG.trace( "Received PrimaryUrlForAssetFailedEvent for asset being updated" );
				requestAttributes.setAttribute( getClass().getName() + ".failureEvent", event, RequestAttributes.SCOPE_REQUEST );
			}
		}
	}

	@Override
	protected void preProcess( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command ) {
		WebCmsAsset asset = entityViewRequest.getCommand().getEntity( WebCmsAsset.class );
		RequestContextHolder.currentRequestAttributes().setAttribute( getClass().getName(), asset, RequestAttributes.SCOPE_REQUEST );
	}

	@Override
	public void postProcess( EntityViewRequest entityViewRequest, EntityView entityView ) {
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		requestAttributes.removeAttribute( getClass().getName(), RequestAttributes.SCOPE_REQUEST );
		requestAttributes.removeAttribute( getClass().getName() + ".failureEvent", RequestAttributes.SCOPE_REQUEST );
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() && entityView.isRedirect() ) {
			PrimaryUrlForAssetFailedEvent event = (PrimaryUrlForAssetFailedEvent) RequestContextHolder
					.currentRequestAttributes()
					.getAttribute( getClass().getName() + ".failureEvent", RequestAttributes.SCOPE_REQUEST );

			if ( event != null && event.isFailed() ) {
				LOG.warn( "Redirecting to primary URL failed form - automatic update of primary url for {} has failed", command.getEntity() );
				String requestedUrl = event.getModificationReport().hasNewValue() ? event.getModificationReport().getNewValue().getPath() : null;

				entityView.setRedirectUrl(
						UriComponentsBuilder.fromUriString( entityViewRequest.getEntityViewContext().getLinkBuilder().update( command.getEntity() ) )
						                    .queryParam( "view", "primaryUrlFailed" )
						                    .queryParam( "requestedUrl", requestedUrl )
						                    .toUriString()
				);
			}
		}
	}
}
