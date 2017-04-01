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

package com.foreach.across.modules.webcms.config.web.admin;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
public class WebCmsArticleConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsArticle.class )
		        .properties( props -> props
				        .property( "assetId" ).hidden( true ).and()
				        .property( "body" )
				        .<HtmlViewElement>viewElementPostProcessor(
						        ViewElementMode.FORM_WRITE,
						        ( builderContext, element ) -> {
							        element.setAttribute( "placeholder", "Some placeholder text..." );
							        element.addCssClass( "js-ckeditor" );
							        WebResourceRegistry registry = builderContext.getAttribute( WebResourceRegistry.class );

							        registry.addWithKey( WebResource.JAVASCRIPT, "ckeditor", "https://cdn.ckeditor.com/4.6.2/full-all/ckeditor.js",
							                             WebResource.EXTERNAL );
							        registry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "custom-js", "/static/WebCmsModule/js/wcm-components.js",
							                             WebResource.VIEWS );

						        } )
		        )
		        .createOrUpdateFormView( fvb -> fvb
				        .properties( props -> props.property( "body" ).viewElementType( ViewElementMode.FORM_WRITE, BootstrapUiElements.TEXTAREA ) )
				        //.properties( props -> props.property( "title" ).viewElementType( ViewElementMode.FORM_WRITE, BootstrapUiElements.TEXTAREA ) )
				        .postProcess( SingleEntityFormViewProcessor.class, processor -> processor.setGrid( Grid.create( 9, 3 ) ) )
				        .viewProcessor( new EntityViewProcessorAdapter()
				        {
					        @Override
					        protected void postRender( EntityViewRequest entityViewRequest,
					                                   EntityView entityView,
					                                   ContainerViewElement container,
					                                   ViewElementBuilderContext builderContext ) {
						        ContainerViewElementUtils.move( container, "formGroup-publication", SingleEntityFormViewProcessor.RIGHT_COLUMN );
						        ContainerViewElementUtils.move( container, "formGroup-title", SingleEntityFormViewProcessor.RIGHT_COLUMN );
						        ContainerViewElementUtils.move( container, "formGroup-subTitle", SingleEntityFormViewProcessor.RIGHT_COLUMN );
						        ContainerViewElementUtils.move( container, "formGroup-description", SingleEntityFormViewProcessor.RIGHT_COLUMN );
						        ContainerViewElementUtils.move( container, "formGroup-created", SingleEntityFormViewProcessor.RIGHT_COLUMN );
						        ContainerViewElementUtils.move( container, "formGroup-lastModified", SingleEntityFormViewProcessor.RIGHT_COLUMN );
					        }
				        } )
		        )
		;
	}
}
