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

import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.image.component.ImageWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.modules.webcms.web.thymeleaf.WebCmsComponentModelRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector.ORIGINAL_HEIGHT;
import static com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector.ORIGINAL_WIDTH;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class ImageWebCmsComponentModelRenderer implements WebCmsComponentModelRenderer<ImageWebCmsComponentModel>
{
	private final WebCmsImageConnector imageConnector;

	@Override
	public boolean supports( WebCmsComponentModel componentModel ) {
		return ImageWebCmsComponentModel.class.isInstance( componentModel );
	}

	@Override
	public void writeComponent( ImageWebCmsComponentModel component, ThymeleafModelBuilder model ) {
		if ( component.hasImage() ) {
			model.addOpenElement( "img" );
			model.addAttribute( "src", imageConnector.buildImageUrl( component.getImage(), ORIGINAL_WIDTH, ORIGINAL_HEIGHT ) );
			model.addCloseElement();
		}
	}
}
