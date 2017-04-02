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

package com.foreach.across.modules.webcms.web.asset;

import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SortableTableRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;

import java.util.Date;

/**
 * Highlights the table rows based on publish status of the asset.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class WebCmsAssetListViewProcessor extends EntityViewProcessorAdapter
{
	@Override
	protected void createViewElementBuilders( EntityViewRequest entityViewRequest, EntityView entityView, ViewElementBuilderMap builderMap ) {
		builderMap.get( SortableTableRenderingViewProcessor.TABLE_BUILDER, SortableTableBuilder.class )
		          .valueRowProcessor( ( builderContext, row ) -> {
			          WebCmsAsset asset = EntityViewElementUtils.currentEntity( builderContext, WebCmsAsset.class );

			          if ( !asset.isPublished() ) {
				          row.setStyle( Style.WARNING );

				          // was published but is offline
				          if ( asset.getPublicationDate() != null && asset.getPublicationDate().before( new Date() ) ) {
					          row.setStyle( Style.DANGER );
				          }
			          }
			          else if ( asset.getPublicationDate() == null || asset.getPublicationDate().before( new Date() ) ) {
				          row.setStyle( Style.SUCCESS );
			          }
			          else {
				          row.setStyle( Style.INFO );
			          }
		          } );
	}

	@Override
	protected void registerWebResources( EntityViewRequest entityViewRequest, EntityView entityView, WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addWithKey( WebResource.CSS, "wcm-styles", "/static/WebCmsModule/css/wcm-styles.css", WebResource.VIEWS );
	}
}
