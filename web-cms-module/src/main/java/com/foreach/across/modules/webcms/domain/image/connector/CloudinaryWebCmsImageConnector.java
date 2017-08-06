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

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Slf4j
@RequiredArgsConstructor
public class CloudinaryWebCmsImageConnector implements WebCmsImageConnector
{
	private final Cloudinary cloudinary;

	@SneakyThrows
	@Override
	public boolean saveImageData( WebCmsImage image, byte[] data ) {
		if ( image.isNew() ) {
			Map result = cloudinary.uploader().upload( data, ObjectUtils.asMap() );
			image.setExternalId( (String) result.get( "public_id" ) );
			return true;
		}

		return false;
	}

	@Override
	public String buildImageUrl( WebCmsImage image, int boxWidth, int boxHeight ) {
		Transformation t = new Transformation();
		if ( boxWidth > ORIGINAL_WIDTH ) {
			t.width( boxWidth );
		}
		if ( boxHeight > ORIGINAL_HEIGHT ) {
			t.height( boxHeight );
		}
		return cloudinary.url()
		                 .format( "jpg" )
		                 .transformation( t.crop( "limit" ) )
		                 .generate( image.getExternalId() );
	}
}
