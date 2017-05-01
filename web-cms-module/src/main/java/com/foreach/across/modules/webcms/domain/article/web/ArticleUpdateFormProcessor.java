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

package com.foreach.across.modules.webcms.domain.article.web;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
final class ArticleUpdateFormProcessor extends EntityViewProcessorAdapter
{
	private final BootstrapUiFactory bootstrapUiFactory;

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {
		container.find( SingleEntityFormViewProcessor.RIGHT_COLUMN, ContainerViewElement.class )
		         .ifPresent( target -> {
			         target.addChild(
					         bootstrapUiFactory.row()
					                           .add( bootstrapUiFactory.column( Grid.Device.MD.width( 6 ) ).name( "pub-metadata-left" ) )
					                           .add( bootstrapUiFactory.column( Grid.Device.MD.width( 6 ) ).name( "pub-metadata-right" ) )
					                           .build( builderContext )
			         );

			         container
					         .removeAllFromTree(
							         "formGroup-title",
							         "formGroup-subTitle",
							         "formGroup-description",
							         "publish-settings",
							         "formGroup-lastModified"
					         )
					         .forEach( target::addChild );

			         ContainerViewElementUtils.move( container, "formGroup-publication", "pub-metadata-left" );
			         ContainerViewElementUtils.move( container, "formGroup-articleType", "pub-metadata-right" );

		         } );
	}
}
