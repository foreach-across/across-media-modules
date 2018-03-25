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

package com.foreach.across.modules.webcms.domain.image.web;

import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.ColumnViewElement;
import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.links.EntityViewLinkBuilder;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.web.menu.RequestMenuSelector;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.modules.webcms.web.ImageWebCmsComponentAdminResources;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * This processor handles the file upload of the form. First, it adds an image extension.  Second, the entity form type is set to multipart/form-data
 * and lastly, the fileupload is handled by {@link WebCmsImageConnector}.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnAdminUI
public class WebCmsImageFormViewProcessor extends EntityViewProcessorAdapter
{
	private final WebCmsImageConnector imageConnector;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		command.addExtension( "image", new ImageHolder() );
	}

	@Override
	protected void validateCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, Errors errors, HttpMethod httpMethod ) {
		if ( HttpMethod.POST.equals( httpMethod ) ) {
			WebCmsImage image = command.getEntity( WebCmsImage.class );

			if ( image.isNew() ) {
				ImageHolder imageHolder = command.getExtension( "image", ImageHolder.class );

				if ( !imageHolder.hasImageData() ) {
					errors.rejectValue( "extensions[image].imageData", "NotNull" );
				}
			}
		}
	}

	@Override
	protected void registerWebResources( EntityViewRequest entityViewRequest, EntityView entityView, WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addPackage( ImageWebCmsComponentAdminResources.NAME );
	}

	@Override
	protected void preProcess( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command ) {
		BindingResult bindingResult = entityViewRequest.getBindingResult();
		if ( entityViewRequest.getHttpMethod().equals( HttpMethod.POST ) && bindingResult != null && !bindingResult.hasErrors() ) {
			ImageHolder imageHolder = command.getExtension( "image", ImageHolder.class );
			WebCmsImage image = command.getEntity( WebCmsImage.class );

			if ( imageHolder.hasImageData() ) {
				try {
					imageConnector.saveImageData( image, imageHolder.getImageData().getBytes() );
					image.setPublished( true );
				}
				catch ( Exception e ) {
					LOG.error( "Unable to upload file", e );
					throw new RuntimeException( e );
				}
			}
		}
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() && Boolean.parseBoolean( entityViewRequest.getWebRequest().getParameter( "imageSelector" ) ) ) {
			val linkBuilder = entityViewRequest.getEntityViewContext().getLinkBuilder();
			WebCmsImage imageCreated = command.getEntity( WebCmsImage.class );

			// redirect to list view filtered on uploaded image
			entityView.setRedirectUrl(
					linkBuilder.listView()
					           .withQueryParam( "extensions[eqFilter]", "id = " + imageCreated.getId() )
					           .withPartial( entityViewRequest.getPartialFragment() )
					           .toUriString()
			);
		}
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		PageContentStructure page = entityViewRequest.getPageContentStructure();
		page.setRenderAsTabs( true );
		page.addCssClass( "wcm-image" );

		if ( !entityViewRequest.getEntityViewContext().holdsEntity() ) {
			page.addCssClass( "wcm-image-upload" );
		}

		val linkBuilder = entityViewRequest.getEntityViewContext().getLinkBuilder();

		if ( !entityViewRequest.getEntityViewContext().holdsEntity() ) {
			page.getHeader().clearChildren();

			Menu menu = buildImageMenu( entityViewRequest, linkBuilder );
			page.withNav( nav -> nav.addFirstChild( BootstrapUiBuilders.nav( menu ).pills().build() ) );
		}

		ContainerViewElementUtils.find( container, "entityForm", FormViewElement.class )
		                         .ifPresent( form -> {
			                         form.setHtmlId( "wcm-image-upload-form" );
			                         form.setEncType( FormViewElement.ENCTYPE_MULTIPART );
		                         } );

		container.find( SingleEntityFormViewProcessor.RIGHT_COLUMN, ColumnViewElement.class )
		         .ifPresent( column ->
				                     container.removeAllFromTree( "formGroup-name", "formGroup-externalId", "formGroup-lastModified" )
				                              .forEach( column::addChild )
		         );
	}

	static Menu buildImageMenu( EntityViewRequest entityViewRequest, EntityViewLinkBuilder linkBuilder ) {
		Menu menu = new PathBasedMenuBuilder()
				.item( "/details", "Search images", linkBuilder.toUriString() ).order( 1 ).and()
				.item( "/associations", "Upload new image", linkBuilder.createView().toUriString() ).order( 2 ).and()
				.build();
		menu.sort();
		menu.select( new RequestMenuSelector( entityViewRequest.getWebRequest().getNativeRequest( HttpServletRequest.class ) ) );
		return menu;
	}

	@Data
	static class ImageHolder
	{
		private MultipartFile imageData;

		boolean hasImageData() {
			return imageData != null && !imageData.isEmpty();
		}
	}
}
