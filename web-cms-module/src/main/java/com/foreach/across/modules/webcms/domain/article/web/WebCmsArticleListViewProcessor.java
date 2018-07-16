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

import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SortableTableRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;

/**
 * If publication is offline - highlights all articles as offline as well.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class WebCmsArticleListViewProcessor extends EntityViewProcessorAdapter
{
	@Override
	protected void createViewElementBuilders( EntityViewRequest entityViewRequest, EntityView entityView, ViewElementBuilderMap builderMap ) {
		if ( builderMap.containsKey( SortableTableRenderingViewProcessor.TABLE_BUILDER ) ) {
			builderMap.get( SortableTableRenderingViewProcessor.TABLE_BUILDER, SortableTableBuilder.class )
			          .valueRowProcessor( ( builderContext, row ) -> {
				          WebCmsArticle asset = EntityViewElementUtils.currentEntity( builderContext,
				                                                                      WebCmsArticle.class );
				          if ( !asset.getPublication().isPublished() ) {
					          row.setStyle( Style.DANGER );
					          row.addCssClass( "publication-offline" );
				          }
			          } );

		}
	}
}
