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

import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.entity.web.EntityViewModel;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class WebCmsUrlEndpointViewElementBuilder implements ViewElementBuilder<ViewElement>
{
	private final EntityRegistry entityRegistry;

	@Override
	public ViewElement build( ViewElementBuilderContext viewElementBuilderContext ) {
		val url = EntityViewElementUtils.currentEntity( viewElementBuilderContext, WebCmsUrl.class );
		val endpoint = url.getEndpoint();

		if ( endpoint != null ) {
			val endpointConfiguration = entityRegistry.getEntityConfiguration( endpoint );

			if ( endpointConfiguration != null && endpointConfiguration.hasAttribute( "endpointValueBuilder" ) ) {
				val nestedBuilderContext = new DefaultViewElementBuilderContext( viewElementBuilderContext );
				nestedBuilderContext.setAttribute( EntityViewModel.ENTITY, endpoint );

				return endpointConfiguration.getAttribute( "endpointValueBuilder", ViewElementBuilder.class ).build( nestedBuilderContext );
			}
		}

		return new ContainerViewElement();
	}
}
