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

package com.foreach.across.modules.webcms.web.asset.processors;

import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.web.menu.RequestMenuSelector;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.image.ImageOwner;
import com.foreach.imageserver.client.ImageServerClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * This processor handles the file upload of the form. First, it adds an image extension.  Second, the entity form type is set to multipart/form-data
 * and lastly,  the fileupload is handled by {@link ImageServerClient}
 *
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@Slf4j
public abstract class ImageFormViewProcessor<T extends ImageOwner> extends EntityViewProcessorAdapter
{
	protected final BeanFactory beanFactory;

	public ImageFormViewProcessor( BeanFactory beanFactory ) {
		this.beanFactory = beanFactory;
	}

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
			T imageOwner = (T) command.getEntity();

			if ( !imageHolder.getImageData().isEmpty() ) {
				String externalId = UUID.randomUUID().toString();

				ImageServerClient imageServerClient = beanFactory.getBean( ImageServerClient.class );
				processImageHolder( imageOwner, externalId );
				try {
					imageServerClient.loadImage( externalId, imageHolder.getImageData().getBytes() );
				}
				catch ( Exception e ) {
					LOG.error( "Unable to upload file", e );
					throw new RuntimeException( e );
				}
			}
		}

	}

	protected abstract void processImageHolder( T imageOwner, String externalId );

	@Data
	static class ImageHolder
	{
		@NotNull
		private MultipartFile imageData;
	}
}
