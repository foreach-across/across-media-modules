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
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.web.menu.RequestMenuSelector;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.web.ImageFormViewProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@ConditionalOnBean(BootstrapUiComponentFactory.class)
@Component
public class WebCmsImageFormViewProcessor extends ImageFormViewProcessor<WebCmsImage>
{
	@Autowired
	private BootstrapUiComponentFactory bootstrapUiComponentFactory;

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		PageContentStructure page = entityViewRequest.getPageContentStructure();
		page.getNav().clearChildren();
		page.setRenderAsTabs( false );

		EntityLinkBuilder linkBuilder = entityViewRequest.getEntityViewContext().getLinkBuilder();

		if ( !entityViewRequest.getEntityViewContext().holdsEntity() ) {
			page.getHeader().clearChildren();

			Menu menu = new PathBasedMenuBuilder()
					.item( "/details", "Search images", linkBuilder.overview() ).order( 1 ).and()
					.item( "/associations", "Upload new image", linkBuilder.create() ).order( 2 ).and()
					.build();
			menu.sort();
			menu.select( new RequestMenuSelector( entityViewRequest.getWebRequest().getNativeRequest( HttpServletRequest.class ) ) );

			page.addToNav( bootstrapUiComponentFactory.nav( menu ).pills().build() );
		}

		super.postRender( entityViewRequest, entityView, container, builderContext );
	}
}
