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

import com.foreach.across.modules.webcms.data.AbstractWebCmsPropertyDataCollectionsImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import org.springframework.stereotype.Component;

/**
 * {@link AbstractWebCmsPropertyDataCollectionsImporter} to re-dispatch menu items imports on a {@link WebCmsMenu}.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
public class WebCmsMenuItemCollectionsImporter extends AbstractWebCmsPropertyDataCollectionsImporter
{
	@Override
	public boolean supports( Phase phase, WebCmsDataEntry dataEntry, Object asset, WebCmsDataAction action ) {
		return WebCmsMenuItemImporter.PROPERTY_NAME.equals( dataEntry.getKey() ) && asset instanceof WebCmsMenu;
	}
}
