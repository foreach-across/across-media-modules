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

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Default {@link AbstractWebCmsPropertyDataCollectionsImporter} that re-dispatches {@link com.foreach.across.modules.webcms.domain.WebCmsObject} imports
 * whose property key contains ":" and could not be resolved by other {@link WebCmsPropertyDataImporter}s.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
@Order
public class WebCmsPropertyDataCollectionsImporter extends AbstractWebCmsPropertyDataCollectionsImporter
{
	@Override
	public boolean supports( Phase phase,
	                         WebCmsDataEntry dataEntry,
	                         Object asset,
	                         WebCmsDataAction action ) {
		return StringUtils.contains( dataEntry.getKey(), ":" ) || StringUtils.startsWith( dataEntry.getKey(), "#" );
	}
}
