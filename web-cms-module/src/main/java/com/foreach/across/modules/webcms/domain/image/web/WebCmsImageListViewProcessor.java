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
import com.foreach.across.modules.bootstrapui.components.BootstrapUiComponentFactory;
import com.foreach.across.modules.bootstrapui.elements.TableViewElement;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.modules.webcms.web.ImageWebCmsComponentAdminResources;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class WebCmsImageListViewProcessor extends EntityViewProcessorAdapter
{
	private final WebCmsImageConnector imageConnector;
	private final BootstrapUiComponentFactory bootstrapUiComponentFactory;

	@Override
	protected void registerWebResources( EntityViewRequest entityViewRequest, EntityView entityView, WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addPackage( ImageWebCmsComponentAdminResources.NAME );
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       ViewElementBuilderContext builderContext ) {
		PageContentStructure page = entityViewRequest.getPageContentStructure();
		EntityLinkBuilder linkBuilder = entityViewRequest.getEntityViewContext().getLinkBuilder();

		page.addCssClass( "wcm-image" );

		Menu menu = WebCmsImageFormViewProcessor.buildImageMenu( entityViewRequest, linkBuilder );
		page.addToNav( bootstrapUiComponentFactory.nav( menu ).pills().build() );
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		Function<WebCmsImage, String> urlGenerator = ( image ) -> imageConnector.buildImageUrl( image, 188, 154 );
		entityView.addAttribute( "imageUrlBuilder", urlGenerator );

		ContainerViewElementUtils.find( container, "itemsTable-table", TableViewElement.class )
		                         .ifPresent( table -> table.setCustomTemplate( "th/webCmsModule/test-admin-images :: content" ) );
	}
}
