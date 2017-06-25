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

import com.foreach.across.modules.bootstrapui.elements.FormGroupElement;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
public class WebCmsAssetUrlFormProcessor extends EntityViewProcessorAdapter
{
	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		ContainerViewElementUtils
				.find( container, "formGroup-primaryLocked", FormGroupElement.class )
				.ifPresent( group -> {
					Map<String, Object> options = new HashMap<>();
					options.put( "hide", true );

					Map<String, Object> qualifiers = new HashMap<>();
					qualifiers.put( "checked", true );

					Map<String, Object> attributes = new HashMap<>();
					attributes.put( "[id='entity.primary']", qualifiers );
					attributes.put( "options", options );

					group.setAttribute( "data-dependson", attributes );
				} );
	}
}
