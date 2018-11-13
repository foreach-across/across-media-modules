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

package com.foreach.across.modules.webcms.web;

import com.foreach.across.modules.web.resource.SimpleWebResourcePackage;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
public class ImageWebCmsComponentAdminResources extends SimpleWebResourcePackage
{
	public static final String NAME = "wcm-image-components-admin";

	public ImageWebCmsComponentAdminResources( WebResourcePackageManager adminWebResourcePackageManager ) {
		adminWebResourcePackageManager.register( NAME, this );

		setDependencies( WebCmsComponentAdminResources.NAME );

		setWebResources(
				new WebResource( WebResource.CSS, NAME, "/static/WebCmsModule/css/wcm-admin-image-component-styles.css", WebResource.VIEWS ),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME, "/static/WebCmsModule/js/wcm-admin-image-components.js", WebResource.VIEWS ),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, "entityQueryFilterForm", "/static/entity/js/entity-query.js", WebResource.VIEWS ),
				new WebResource( WebResource.JAVASCRIPT, "lodash", "https://cdnjs.cloudflare.com/ajax/libs/lodash.js/4.17.4/lodash.min.js",
				                 WebResource.EXTERNAL )
		);
	}
}
