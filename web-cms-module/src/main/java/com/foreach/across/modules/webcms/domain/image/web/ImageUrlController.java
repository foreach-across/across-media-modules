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

import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.WebCmsImageRepository;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AdminWebController
@ResponseBody
@RequiredArgsConstructor
class ImageUrlController
{
	private final BeanFactory beanFactory;
	private final WebCmsImageRepository imageRepository;

	@GetMapping("/utils/buildImageUrl")
	public String buildImageUrl( @RequestParam("width") int width, @RequestParam("height") int height, @RequestParam("imageId") WebCmsImage image ) {
		ImageServerClient imageServerClient = beanFactory.getBean( ImageServerClient.class );

		ImageVariantDto variant = new ImageVariantDto();
		variant.setBoundaries( new DimensionsDto( width, height ) );
		variant.setImageType( ImageTypeDto.PNG );

		return imageServerClient.imageUrl( image.getExternalId(), "default", 0, 0, variant );
	}
}
