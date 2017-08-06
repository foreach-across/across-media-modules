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

import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.image.ImageOwner;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * This processor handles the file upload of the form. First, it adds an image extension.  Second, the entity form type is set to multipart/form-data
 * and lastly,  the fileupload is handled by {@link WebCmsImageConnector}.
 *
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@Slf4j
public abstract class ImageFormViewProcessor<T extends ImageOwner> extends EntityViewProcessorAdapter
{
	private WebCmsImageConnector imageConnector;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		command.addExtension( "image", new ImageHolder() );
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		ContainerViewElementUtils.find( container, "entityForm", FormViewElement.class )
		                         .ifPresent( form -> form.setEncType( FormViewElement.ENCTYPE_MULTIPART ) );
	}

	@Override
	protected void preProcess( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command ) {
		BindingResult bindingResult = entityViewRequest.getBindingResult();
		if ( entityViewRequest.getHttpMethod().equals( HttpMethod.POST ) && bindingResult != null && !bindingResult.hasErrors() ) {

			ImageHolder imageHolder = command.getExtension( "image", ImageHolder.class );
			WebCmsImage image = command.getEntity( WebCmsImage.class );

			if ( !imageHolder.getImageData().isEmpty() ) {
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

	@Autowired
	public final void setImageConnector( WebCmsImageConnector imageConnector ) {
		this.imageConnector = imageConnector;
	}

	@Data
	static class ImageHolder
	{
		@NotNull
		private MultipartFile imageData;
	}
}
