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

import com.foreach.across.modules.webcms.domain.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Simple importer implementation that will set the current domain scope.
 * Can be present in a data collection as the {@code wcm:domain} key.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
public class WebCmsDomainContextPropertyImporter implements WebCmsPropertyDataImporter
{
	private final StringToWebCmsDomainConverter domainConverter;
	private final WebCmsMultiDomainService multiDomainService;

	public static final String DOMAIN = "wcm:domain";

	@Override
	public boolean supports( Phase phase,
	                         String propertyName,
	                         Object asset,
	                         WebCmsDataAction action ) {
		return Phase.BEFORE_ASSET_SAVED.equals( phase ) && DOMAIN.equals( propertyName ) && asset instanceof WebCmsDomainBound;
	}

	@Override
	public boolean importData( Phase phase,
	                           WebCmsDataEntry propertyData,
	                           Object asset,
	                           WebCmsDataAction action ) {
		String domainKey = (String) propertyData.getSingleValue();

		WebCmsDomain domain = domainKey != null
				? Optional.ofNullable( domainConverter.convert( domainKey ) )
				          .orElseThrow( () -> new IllegalArgumentException( "No domain with key '" + domainKey + "' was found" ) )
				: WebCmsDomain.NONE;

		CloseableWebCmsDomainContext ctx = multiDomainService.attachDomainContext( domain );
		if ( propertyData.hasParent() ) {
			propertyData.getParent().addCompletedCallback( dataEntry -> ctx.close() );
		}
		else {
			propertyData.addCompletedCallback( dataEntry -> ctx.close() );
		}
		return true;
	}
}
