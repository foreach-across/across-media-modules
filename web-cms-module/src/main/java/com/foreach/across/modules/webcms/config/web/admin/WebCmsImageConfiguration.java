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

package com.foreach.across.modules.webcms.config.web.admin;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.bootstrapui.elements.TableViewElement;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.dto.ImageTypeDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@RequiredArgsConstructor
public class WebCmsImageConfiguration implements EntityConfigurer
{
	private final CreateFormProcessor createFormProcessor;
	private final ThumbnailViewElementBuilder thumbnailViewElementBuilder;
	private final ListViewProcessor listViewProcessor;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsImage.class )
		        .properties(
				        props -> props.property( "publish-settings" ).hidden( true ).and()
				                      .property( "assetId" ).hidden( true ).and()
				                      .property( "externalId" ).writable( false ).and()
				                      .property( "image-upload" )
				                      .displayName( "Image file" )
				                      .hidden( true )
				                      .writable( true )
				                      .readable( false )
				                      .viewElementBuilder(
						                      ViewElementMode.CONTROL,
						                      builderContext -> {
							                      NodeViewElement fileUpload = new NodeViewElement( "input" );
							                      fileUpload.setAttribute( "type", "file" );
							                      fileUpload.addCssClass( "form-control" );
							                      fileUpload.setAttribute( "name", "extensions[image].imageData" );
							                      return fileUpload;
						                      }
				                      )
		        )
		        .createFormView(
				        fvb -> fvb.showProperties( ".", "image-upload" )
				                  .postProcess( ( factory, processors ) -> {
					                  // ensure image uploading happens before saving the image record
					                  processors.addProcessor( createFormProcessor, 0 );
				                  } )
		        )
		        .updateFormView(
				        fvb -> fvb.properties( props -> props.property( "thumbnail" )
				                                             .writable( true )
				                                             .viewElementBuilder( ViewElementMode.FORM_WRITE, thumbnailViewElementBuilder ) )
				                  .showProperties( ".", "thumbnail" )
		        )
		        .listView(
				        lvb -> lvb.viewProcessor( listViewProcessor )

		        )

		;
	}

	@Component
	private static class ListViewProcessor extends EntityViewProcessorAdapter
	{
		@Override
		protected void postRender( EntityViewRequest entityViewRequest,
		                           EntityView entityView,
		                           ContainerViewElement container,
		                           ViewElementBuilderContext builderContext ) {
			ContainerViewElementUtils.find( container, "table", TableViewElement.class )
			                         .ifPresent( table -> table.setCustomTemplate( "th/webCmsModule/test-admin-images :: content" ) );
		}
	}

	@Component
	@RequiredArgsConstructor
	public static class ThumbnailViewElementBuilder implements ViewElementBuilder<ViewElement>
	{
		private final BootstrapUiFactory bootstrapUiFactory;
		private final BeanFactory beanFactory;

		@Override
		public ViewElement build( ViewElementBuilderContext viewElementBuilderContext ) {
			ImageServerClient imageServerClient = beanFactory.getBean( ImageServerClient.class );
			WebCmsImage image = EntityViewElementUtils.currentEntity( viewElementBuilderContext, WebCmsImage.class );

			return bootstrapUiFactory.node( "img" )
			                         .attribute( "src", imageServerClient.imageUrl( image.getExternalId(), "default", 0, 0, ImageTypeDto.PNG ) )
			                         .build( viewElementBuilderContext );
		}
	}

	@Component
	@RequiredArgsConstructor
	public static class CreateFormProcessor extends EntityViewProcessorAdapter
	{
		private final BeanFactory beanFactory;

		@Override
		public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
			command.addExtension( "image", new ImageHolder() );
		}

		@Override
		protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
			if ( !bindingResult.hasErrors() ) {

				ImageHolder imageHolder = command.getExtension( "image", ImageHolder.class );
				WebCmsImage image = command.getEntity( WebCmsImage.class );

				if ( imageHolder.getImageData().isEmpty() ) {
					bindingResult.reject( "noImage", "Please select a valid image." );
				}
				else {
					String externalId = UUID.randomUUID().toString();
					image.setExternalId( externalId );

					ImageServerClient imageServerClient = beanFactory.getBean( ImageServerClient.class );
					try {
						imageServerClient.loadImage( externalId, imageHolder.getImageData().getBytes() );
					}
					catch ( Exception e ) {
						throw new RuntimeException( e );
					}
				}
			}
		}

		@Override
		protected void postRender( EntityViewRequest entityViewRequest,
		                           EntityView entityView,
		                           ContainerViewElement container,
		                           ViewElementBuilderContext builderContext ) {
			ContainerViewElementUtils.find( container, "entityForm", FormViewElement.class )
			                         .ifPresent( form -> form.setEncType( FormViewElement.ENCTYPE_MULTIPART ) );
		}
	}

	@Data
	static class ImageHolder
	{
		@NotNull
		private MultipartFile imageData;
	}
}
