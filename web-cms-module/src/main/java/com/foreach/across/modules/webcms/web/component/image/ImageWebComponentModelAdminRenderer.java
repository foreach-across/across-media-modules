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

package com.foreach.across.modules.webcms.web.component.image;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModel;
import com.foreach.across.modules.webcms.domain.image.component.ImageWebComponentModel;
import com.foreach.across.modules.webcms.web.component.WebComponentModelAdminRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AcrossDepends(required = AdminWebModule.NAME)
@Component
@RequiredArgsConstructor
public class ImageWebComponentModelAdminRenderer implements WebComponentModelAdminRenderer<ImageWebComponentModel>
{
	private final BootstrapUiFactory bootstrapUiFactory;

	@Override
	public boolean supports( WebComponentModel componentModel ) {
		return ImageWebComponentModel.class.isInstance( componentModel );
	}

	@Override
	public ViewElementBuilder createContentViewElementBuilder( ImageWebComponentModel componentModel, String controlNamePrefix ) {
		return bootstrapUiFactory
				.formGroup()
				.label( bootstrapUiFactory.label( componentModel.getTitle() ).attribute( "title", componentModel.getName() ) )
				.control(

						bootstrapUiFactory
								.div()
								.attribute( "data-wcm-component-type", componentModel.getComponentType().getTypeKey() )
								.attribute( "data-wcm-component-base-type", "image" )
								.add(
										bootstrapUiFactory.hidden()
										                  .controlName( controlNamePrefix + ".image" )
										                  .value( componentModel.hasImage() ? componentModel.getImage().getObjectId() : null )
								)
								.add(
										bootstrapUiFactory.button()
										                  .name( "btn-select-image" )
										                  .style( Style.PRIMARY )
										                  .text( "Select image" )
								)
				)
				.postProcessor( ( builderContext, formGroup ) -> {
					WebResourceRegistry resourceRegistry = builderContext.getAttribute( WebResourceRegistry.class );
					resourceRegistry.addWithKey( WebResource.CSS, WebCmsModule.NAME, "/static/WebCmsModule/css/wcm-styles.css", WebResource.VIEWS );
					resourceRegistry.addWithKey(
							WebResource.JAVASCRIPT_PAGE_END,
							"bootbox",
							"https://cdnjs.cloudflare.com/ajax/libs/bootbox.js/4.4.0/bootbox.min.js",
							WebResource.EXTERNAL );
					resourceRegistry.addWithKey( WebResource.JAVASCRIPT_PAGE_END, "wcm-image-component", "/static/WebCmsModule/js/wcm-image-component.js",
					                             WebResource.VIEWS );
				} );

	}
}
