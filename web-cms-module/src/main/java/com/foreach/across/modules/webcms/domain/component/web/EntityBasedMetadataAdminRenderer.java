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

package com.foreach.across.modules.webcms.domain.component.web;

import com.foreach.across.modules.bootstrapui.elements.FormInputElement;
import com.foreach.across.modules.bootstrapui.elements.processor.ControlNamePrefixingPostProcessor;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderHelper;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.helpers.EntityViewElementBatch;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Fallback metadata renderer that checks if the metadata type is registered as an entity in the {@link EntityRegistry}.
 * If that is the case, will use that entity configuration to render a form, else no attributes will be rendered.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
class EntityBasedMetadataAdminRenderer implements WebCmsComponentModelMetadataAdminRenderer<WebCmsComponentModel, Object>
{
	private final EntityViewElementBuilderHelper builderHelper;
	private final EntityRegistry entityRegistry;

	@Override
	public boolean supports( WebCmsComponentModel componentModel, Object metadata ) {
		return metadata != null && entityRegistry.contains( ClassUtils.getUserClass( metadata ) );
	}

	@Override
	public ViewElementBuilder createMetadataViewElementBuilder( WebCmsComponentModel componentModel, Object metadata, String controlNamePrefix ) {
		EntityViewElementBatch<Object> formBuilder = builderHelper.createBatchForEntity( metadata );
		formBuilder.setViewElementMode( ViewElementMode.FORM_WRITE );

		ContainerViewElementBuilder containerBuilder = new ContainerViewElementBuilder();
		formBuilder.build().forEach( ( name, element ) -> containerBuilder.add( element ) );
		containerBuilder.postProcessor( ( ctx, container ) -> {
			ControlNamePrefixingPostProcessor controlNamePrefixingPostProcessor = new ControlNamePrefixingPostProcessor( controlNamePrefix + ".metadata" );
			container.findAll( FormInputElement.class )
			         .forEach( e -> controlNamePrefixingPostProcessor.postProcess( ctx, e ) );
		} );

		return containerBuilder;
	}
}
