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

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModel;
import com.foreach.across.modules.webcms.domain.image.component.ImageWebComponentModel;
import com.foreach.across.modules.webcms.web.thymeleaf.WebComponentModelRenderer;
import com.foreach.imageserver.client.ImageServerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class ImageWebComponentModelRenderer implements WebComponentModelRenderer<ImageWebComponentModel>
{
	private final BeanFactory beanFactory;

	private ImageServerClient imageServerClient;

	@Override
	public boolean supports( WebComponentModel componentModel ) {
		return ImageWebComponentModel.class.isInstance( componentModel );
	}

	@Override
	public void writeComponent( ImageWebComponentModel component, ThymeleafModelBuilder model ) {
		if ( imageServerClient != null && component.hasImageServerKey() ) {
			model.addOpenElement( "img" );
			model.addAttribute( "src", imageServerClient.imageUrl( component.getImageServerKey(), "default", 0, 0 ) );
			model.addCloseElement();
		}
	}

	@PostRefresh
	void loadImageServerClient() {
		imageServerClient = beanFactory.getBean( ImageServerClient.class );
	}
}
