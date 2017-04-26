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

package com.foreach.across.modules.webcms.web.component;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.bootstrapui.elements.builder.ColumnViewElementBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.model.OrderedWebComponentModelSet;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renders an ordered collection of components as a single form.
 * Expects the entity to be the {@link com.foreach.across.modules.webcms.domain.WebCmsObject} that owns the components.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Exposed
@Scope("prototype")
@RequiredArgsConstructor
public class OrderedWebCmsComponentsFormProcessor extends EntityViewProcessorAdapter
{
	private final WebCmsComponentModelService componentModelService;
	private final WebComponentModelAdminRenderService componentModelAdminRenderService;

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		OrderedWebComponentModelSet components = componentModelService.getComponentModelsForOwner( command.getEntity( WebCmsObject.class ) );

		Map<String, WebCmsComponentModel> extensionMap = new LinkedHashMap<>();
		components.getOrdered().forEach( model -> extensionMap.put( model.getObjectId(), model ) );

		command.addExtension( "webCmsComponents", extensionMap );
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() ) {
			val componentsById = (Map<String, WebCmsComponentModel>) entityViewRequest.getCommand().getExtension( "webCmsComponents", Map.class );
			componentsById.forEach( ( objectId, model ) -> componentModelService.save( model ) );
		}
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       ViewElementBuilderContext builderContext ) {
		val componentsById = (Map<String, WebCmsComponentModel>) entityViewRequest.getCommand().getExtension( "webCmsComponents", Map.class );

		ColumnViewElementBuilder body = builderMap.get( SingleEntityFormViewProcessor.LEFT_COLUMN, ColumnViewElementBuilder.class );
		componentsById.forEach( ( objectId, model ) -> body
				.add( componentModelAdminRenderService.createContentViewElementBuilder( model, "extensions[webCmsComponents]['" + objectId + "']" ) )
		);
		/*
		body
		          .add( componentModelAdminRenderService.createContentViewElementBuilder( componentModel, "extensions[" + EXTENSION_NAME + "]" ) );
		          */
	}
}
