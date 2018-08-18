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

package com.foreach.across.modules.webcms.domain.menu;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link String} to {@link WebCmsMenu}.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public class StringToWebCmsMenuConverter implements Converter<String, WebCmsMenu>
{
	private final WebCmsMenuRepository webCmsMenuRepository;
	private final WebCmsMenuService menuService;

	@Autowired
	void registerToConversionService( WebCmsDataConversionService conversionService ) {
		conversionService.addConverter( this );
	}

	@Override
	public WebCmsMenu convert( String value ) {
		if ( NumberUtils.isDigits( value ) ) {
			return webCmsMenuRepository.findById( Long.parseLong( value ) ).orElse( null );
		}

		if ( WebCmsUtils.isObjectIdForCollection( value, WebCmsMenu.COLLECTION_ID ) ) {
			return webCmsMenuRepository.findOneByObjectId( value );
		}

		return menuService.getMenuByName( value );
	}
}
