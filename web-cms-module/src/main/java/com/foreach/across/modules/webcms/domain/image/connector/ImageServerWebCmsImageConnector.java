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

package com.foreach.across.modules.webcms.domain.image.connector;

import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageInfoDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RequiredArgsConstructor
public class ImageServerWebCmsImageConnector implements WebCmsImageConnector
{
	private final ImageServerClient imageServerClient;

	@Setter
	private String context = "default";

	@Setter
	private boolean performDeletes = true;

	@Override
	public boolean saveImageData( WebCmsImage image, byte[] data ) {
		if ( image.isNew() ) {
			String externalId = UUID.randomUUID().toString();
			ImageInfoDto imageInfo = imageServerClient.loadImage( externalId, data );
			image.setExternalId( imageInfo.getExternalId() );
			return true;
		}

		return false;
	}

	@Override
	public String buildImageUrl( WebCmsImage image, int boxWidth, int boxHeight ) {
		ImageVariantDto variant = new ImageVariantDto();
		variant.setBoundaries( new DimensionsDto( boxWidth, boxHeight ) );
		variant.setImageType( ImageTypeDto.PNG );

		return imageServerClient.imageUrl( image.getExternalId(), context, 0, 0, variant );
	}

	@Override
	public boolean deleteImageData( WebCmsImage image ) {
		if ( performDeletes ) {
			return imageServerClient.deleteImage( image.getExternalId() );
		}

		return false;
	}
}
