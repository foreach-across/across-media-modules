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
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import lombok.RequiredArgsConstructor;
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
	private final WebCmsImageConnector imageConnector;

	@GetMapping("/utils/buildImageUrl")
	public String buildImageUrl( @RequestParam("width") int boxWidth, @RequestParam("height") int boxHeight, @RequestParam("imageId") WebCmsImage image ) {
		return imageConnector.buildImageUrl( image, boxWidth, boxHeight );
	}
}
