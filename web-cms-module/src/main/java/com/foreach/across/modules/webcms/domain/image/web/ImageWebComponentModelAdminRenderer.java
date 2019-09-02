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

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentModelContentAdminRenderer;
import com.foreach.across.modules.webcms.domain.image.component.ImageWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.modules.webcms.web.ImageWebCmsComponentAdminResources;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.webcms.config.icons.WebCmsIcons.webCmsIcons;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAcrossModule(AdminWebModule.NAME)
@Component
@RequiredArgsConstructor
public class ImageWebComponentModelAdminRenderer implements WebCmsComponentModelContentAdminRenderer<ImageWebCmsComponentModel>
{
	private final WebCmsImageConnector imageConnector;

	@Override
	public boolean supports( WebCmsComponentModel componentModel ) {
		return ImageWebCmsComponentModel.class.isInstance( componentModel );
	}

	@Override
	public ViewElementBuilder createContentViewElementBuilder( ImageWebCmsComponentModel componentModel, String controlNamePrefix ) {
		String thumbnailUrl = componentModel.hasImage()
				? imageConnector.buildImageUrl( componentModel.getImage(), 188, 154 )
				: null;

		return BootstrapUiBuilders
				.div()
				.attribute( "data-wcm-component-type", componentModel.getComponentType().getTypeKey() )
				.attribute( "data-wcm-component-base-type", "image" )
				.css( "image-selected-container", "clearfix" )
				.add(
						BootstrapUiBuilders.hidden()
						                   .controlName( controlNamePrefix + ".image" )
						                   .attribute( "data-wcm-component-property", "image" )
						                   .value( componentModel.hasImage() ? componentModel.getImage().getObjectId() : null )
				)
				.add(
						BootstrapUiBuilders.div()
						                   .css( "image-thumbnail-container", thumbnailUrl != null ? "" : "d-none" )
						                   .add(
								                   BootstrapUiBuilders.node( "img" )
								                                      .attribute( "src", thumbnailUrl )
								                                      .attribute( "border", "1" )
						                   )
				)
				.add(
						BootstrapUiBuilders.div()
						                   .css( "image-thumbnail-actions", thumbnailUrl != null ? "" : "d-none" )
						                   .add(
								                   BootstrapUiBuilders.button()
								                                      .link()
								                                      .attribute( "data-wcm-image-action", "edit" )
								                                      .iconOnly( webCmsIcons.image.edit() )
								                                      .text( "Change image" )
						                   )
						                   .add(
								                   BootstrapUiBuilders.button()
								                                      .link()
								                                      .attribute( "data-wcm-image-action", "delete" )
								                                      .iconOnly( webCmsIcons.image.remove() )
								                                      .text( "Remove image" )
						                   )
				)
				.add(
						BootstrapUiBuilders.button()
						                   .name( "btn-select-image" )
						                   .css( thumbnailUrl != null ? "d-none" : "" )
						                   .style( Style.DEFAULT )
						                   .text( "Select image" )
				)
				.postProcessor( ( builderContext, wrapper ) -> {
					builderContext.getAttribute( WebResourceRegistry.class ).addPackage( ImageWebCmsComponentAdminResources.NAME );
				} );
	}
}
