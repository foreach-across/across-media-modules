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

package com.foreach.across.modules.webcms.domain.page.web;

import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageTypeRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for {@link com.foreach.across.modules.webcms.domain.page.WebCmsPageType}.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Component
@Data
@Slf4j
@ConfigurationProperties(prefix = "webCmsModule.pages")
@RequiredArgsConstructor
public class PageTypeProperties
{
	private static final String DEFAULT_PAGE_TYPE_TYPE_KEY = "default";
	private final WebCmsPageTypeRepository webCmsPageTypeRepository;
	private String defaultPageType;

	public WebCmsPageType getDefaultType() {
		String keyOrObjectId = getDefaultPageType();
		if ( StringUtils.isBlank( keyOrObjectId ) ) {
			LOG.warn( String.format( "Default page type is blank, assuming [%s]", PageTypeProperties.DEFAULT_PAGE_TYPE_TYPE_KEY ) );
			keyOrObjectId = PageTypeProperties.DEFAULT_PAGE_TYPE_TYPE_KEY;
		}
		WebCmsPageType defaultPageType = webCmsPageTypeRepository.findOneByTypeKeyOrObjectId( keyOrObjectId, keyOrObjectId );
		if ( defaultPageType == null ) {
			LOG.error( String.format( "Could not resolve default pagetype with objectId [%s]", keyOrObjectId ) );
		}
		return defaultPageType;
	}
}
