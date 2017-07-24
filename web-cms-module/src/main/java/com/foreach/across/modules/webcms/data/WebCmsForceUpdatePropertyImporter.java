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

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Simple importer implementation that will act as if a DTO has been changed in order to force saving the dto.
 * Useful for REPLACE action.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public final class WebCmsForceUpdatePropertyImporter implements WebCmsPropertyDataImporter
{
	public static final String FORCE_UPDATE = "wcm:force-update";

	@Override
	public Phase getPhase() {
		return Phase.BEFORE_ASSET_SAVED;
	}

	@Override
	public boolean supports( WebCmsDataEntry parentData, String propertyName, Object asset, WebCmsDataAction action ) {
		return FORCE_UPDATE.equals( propertyName );
	}

	@Override
	public boolean importData( WebCmsDataEntry parentData, WebCmsDataEntry propertyData, Object asset, WebCmsDataAction action ) {
		// return true as if object has been modified - forces saving a dto
		return true;
	}
}
