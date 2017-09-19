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

package com.foreach.across.modules.webcms.domain.page;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.data.WebCmsDataImportServiceImpl;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter that supports either object id or canonical path for looking up a {@link WebCmsPage}.
 *
 * @author Arne Vandamme
 * @see WebCmsDataImportServiceImpl
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public final class StringToWebCmsPageConverter implements Converter<String, WebCmsPage>
{
	private final WebCmsPageRepository pageRepository;
	private final WebCmsPageService pageService;

	@Autowired
	void register( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( this );
	}

	@Override
	public WebCmsPage convert( String source ) {
		if ( NumberUtils.isDigits( source ) ) {
			return pageRepository.findOne( Long.parseLong( source ) );
		}

		if ( source.startsWith( "/" ) ) {
			return pageService.findByCanonicalPath( source ).orElse( null );
		}

		return pageRepository.findOneByObjectId( source );
	}
}
