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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link String} to {@link WebCmsAsset}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class StringToWebCmsAssetConverter implements ConverterFactory<String, WebCmsAsset>
{
	private final WebCmsAssetRepository assetRepository;
	private final WebCmsPageService pageService;

	@Autowired
	void registerToConversionService( WebCmsDataConversionService conversionService ) {
		conversionService.addConverterFactory( this );
	}

	@Autowired
	void registerToMvcConversionService( @Qualifier(AcrossWebModule.CONVERSION_SERVICE_BEAN) ConverterRegistry mvcConversionService ) {
		mvcConversionService.addConverterFactory( this );
	}

	@Override
	public <T extends WebCmsAsset> Converter<String, T> getConverter( Class<T> targetType ) {
		return ( id ) -> {
			if ( NumberUtils.isDigits( id ) ) {
				return targetType.cast( assetRepository.findOne( Long.parseLong( id ) ) );
			}

			if ( id.startsWith( "/" ) ) {
				return targetType.cast( pageService.findByCanonicalPath( id ).orElse( null ) );
			}

			return StringUtils.isEmpty( id ) ? null : targetType.cast( assetRepository.findOneByObjectId( id ) );
		};
	}
}
