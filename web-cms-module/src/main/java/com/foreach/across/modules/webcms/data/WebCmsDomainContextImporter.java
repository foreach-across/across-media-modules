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

package com.foreach.across.modules.webcms.data;

import com.foreach.across.modules.webcms.domain.domain.CloseableWebCmsDomainContext;
import com.foreach.across.modules.webcms.domain.domain.StringToWebCmsDomainConverter;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class WebCmsDomainContextImporter implements WebCmsDataImporter
{
	private final StringToWebCmsDomainConverter domainConverter;
	private final WebCmsMultiDomainService multiDomainService;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return WebCmsDomainContextPropertyImporter.DOMAIN.equals( data.getKey() );
	}

	@Override
	public void importData( WebCmsDataEntry data ) {
		CloseableWebCmsDomainContext ctx = multiDomainService.attachDomainContext( domainConverter.convert( (String) data.getSingleValue() ) );
		if ( data.hasParent() ) {
			data.getParent().addCompletedCallback( dataEntry -> ctx.close() );
		}
		else {
			data.addCompletedCallback( dataEntry -> ctx.close() );
		}
	}
}
