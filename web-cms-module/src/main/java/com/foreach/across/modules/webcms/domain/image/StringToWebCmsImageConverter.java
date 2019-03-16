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

package com.foreach.across.modules.webcms.domain.image;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.WebCmsObjectNotFoundException;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Converts a string to a {@link WebCmsImage} instance and optionally creates and persists a new {@link WebCmsImage}.
 * <p/>
 * Will check if the string value is a {@link WebCmsImage#objectId} and if so will look for an existing image with that id.
 * If the string does not match the object id pattern, it assumed to be a resource identifier instead.
 * An object id will be generated based on the resource identifier, and an image will be fetched with that object id.
 * If no image can be found, one will be created from the resource.
 * <p/>
 * An exception will be thrown if no image can be found or can be created.
 * <p/>
 * This converter is meant to be used by the {@link com.foreach.across.modules.webcms.data.WebCmsDataImportService}.
 * Because it requires an active {@link WebCmsImageConnector}, it's usually required that the context has been fully bootstrapped
 * before image imports are attempted.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
final class StringToWebCmsImageConverter implements Converter<String, WebCmsImage>
{
	private final ApplicationContext applicationContext;
	private final WebCmsImageConnector imageConnector;
	private final WebCmsImageRepository imageRepository;
	private final WebCmsMultiDomainService multiDomainService;

	@Override
	public WebCmsImage convert( String source ) {
		try {
			WebCmsImage image = retrieveOrCreateImage( source );

			if ( image == null ) {
				throw new WebCmsObjectNotFoundException( source, WebCmsImage.class );
			}

			return image;
		}
		catch ( Exception re ) {
			if ( re instanceof WebCmsObjectNotFoundException ) {
				throw (WebCmsObjectNotFoundException) re;
			}
			else {
				throw new WebCmsObjectNotFoundException( source, WebCmsImage.class, re );
			}
		}
	}

	private WebCmsImage retrieveOrCreateImage( String source ) throws IOException {
		if ( StringUtils.isEmpty( source ) ) {
			return null;

		}
		if ( WebCmsUtils.isObjectIdForCollection( source, WebCmsImage.COLLECTION_ID ) ) {
			return imageRepository.findOneByObjectId( source );
		}

		WebCmsDomain currentDomain = multiDomainService.getCurrentDomainForType( WebCmsImage.class );
		String objectId = generateImageObjectId( source, currentDomain );
		WebCmsImage image = imageRepository.findOneByObjectId( objectId );

		if ( image == null ) {
			Resource imageResource = applicationContext.getResource( source );
			if ( !imageResource.exists() ) {
				throw new IllegalArgumentException( "Image resource does not exist: " + source );
			}

			image = WebCmsImage.builder()
			                   .objectId( objectId )
			                   .domain( currentDomain )
			                   .name( imageResource.getFilename() )
			                   .published( true )
			                   .build();
			byte[] imageData = StreamUtils.copyToByteArray( imageResource.getInputStream() );

			imageConnector.saveImageData( image, imageData );
			imageRepository.save( image );
		}

		return image;
	}

	private String generateImageObjectId( String source, WebCmsDomain domain ) {
		String suffix = "import-" + DigestUtils.md5DigestAsHex( source.getBytes( Charset.forName( "UTF-8" ) ) );

		if ( !WebCmsDomain.isNoDomain( domain ) ) {
			suffix += "-" + DigestUtils.md5DigestAsHex( domain.getObjectId().getBytes() );
		}

		return WebCmsUtils.prefixObjectIdForCollection( suffix, WebCmsImage.COLLECTION_ID );
	}

	@Autowired
	void registerConverter( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( String.class, WebCmsImage.class, this );
	}
}
