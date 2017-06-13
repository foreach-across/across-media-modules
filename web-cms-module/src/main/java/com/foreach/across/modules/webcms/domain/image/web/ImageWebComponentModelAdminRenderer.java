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

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.GlyphIcon;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentModelContentAdminRenderer;
import com.foreach.across.modules.webcms.domain.image.component.ImageWebCmsComponentModel;
import com.foreach.across.modules.webcms.web.ImageWebCmsComponentAdminResources;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AcrossDepends(required = AdminWebModule.NAME)
@Component
@RequiredArgsConstructor
public class ImageWebComponentModelAdminRenderer implements WebCmsComponentModelContentAdminRenderer<ImageWebCmsComponentModel>
{
	private final BeanFactory beanFactory;
	private final BootstrapUiFactory bootstrapUiFactory;

	@Override
	public boolean supports( WebCmsComponentModel componentModel ) {
		return ImageWebCmsComponentModel.class.isInstance( componentModel );
	}

	@Override
	public ViewElementBuilder createContentViewElementBuilder( ImageWebCmsComponentModel componentModel, String controlNamePrefix ) {
		ImageServerClient imageServerClient = beanFactory.getBean( ImageServerClient.class );

		ImageVariantDto variant = new ImageVariantDto();
		variant.setBoundaries( new DimensionsDto( 188, 154 ) );
		variant.setImageType( ImageTypeDto.PNG );

		String thumbnailUrl = componentModel.hasImageServerKey()
				? imageServerClient.imageUrl( componentModel.getImageServerKey(), "default", 0, 0, variant )
				: null;

		return bootstrapUiFactory
				.div()
				.attribute( "data-wcm-component-type", componentModel.getComponentType().getTypeKey() )
				.attribute( "data-wcm-component-base-type", "image" )
				.css( "image-selected-container", "clearfix" )
				.add(
						bootstrapUiFactory.hidden()
						                  .controlName( controlNamePrefix + ".image" )
						                  .attribute( "data-wcm-component-property", "image" )
						                  .value( componentModel.hasImage() ? componentModel.getImage().getObjectId() : null )
				)
				.add(
						bootstrapUiFactory.div()
						                  .css( "image-thumbnail-container", thumbnailUrl != null ? "" : "hidden" )
						                  .add(
								                  bootstrapUiFactory.node( "img" )
								                                    .attribute( "src", thumbnailUrl )
								                                    .attribute( "border", "1" )
						                  )
				)
				.add(
						bootstrapUiFactory.div()
						                  .css( "image-thumbnail-actions", thumbnailUrl != null ? "" : "hidden" )
						                  .add(
								                  bootstrapUiFactory.button()
								                                    .link()
								                                    .attribute( "data-wcm-image-action", "edit" )
								                                    .iconOnly( new GlyphIcon( GlyphIcon.EDIT ) )
								                                    .text( "Change image" )
						                  )
						                  .add(
								                  bootstrapUiFactory.button()
								                                    .link()
								                                    .attribute( "data-wcm-image-action", "delete" )
								                                    .iconOnly( new GlyphIcon( GlyphIcon.REMOVE ) )
								                                    .text( "Remove image" )
						                  )
				)
				.add(
						bootstrapUiFactory.button()
						                  .name( "btn-select-image" )
						                  .css( thumbnailUrl != null ? "hidden" : "" )
						                  .style( Style.DEFAULT )
						                  .text( "Select image" )
				)
				.postProcessor( ( builderContext, wrapper ) -> {
					builderContext.getAttribute( WebResourceRegistry.class ).addPackage( ImageWebCmsComponentAdminResources.NAME );
				} );
	}
}
