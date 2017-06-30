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

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataImportServiceImpl;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * In which we assume that if the lookup string starts with a '/' we look up a page, otherwise we look up a (generic) asset.
 *
 * @author Raf Ceuls
 * @see WebCmsDataImportServiceImpl
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public final class StringToWebCmsAssetConverter implements Converter<String, WebCmsAsset>
{
	private final WebCmsAssetRepository assetRepository;
	private final WebCmsPageRepository pageRepository;

	@Autowired
	void register( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( this );
	}

	@Override
	public WebCmsAsset convert( String source ) {
		if ( source.startsWith( "/" ) ) {
			return pageRepository.findOneByCanonicalPath( source );
		}

		return assetRepository.findOneByObjectId( source );
	}
}
