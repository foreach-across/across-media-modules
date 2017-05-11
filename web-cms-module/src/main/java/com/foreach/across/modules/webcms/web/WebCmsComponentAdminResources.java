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
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.springframework.stereotype.Component;

/**
 * Contains the general web resources to enable the administration UI for {@link WebCmsComponentModel}s.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
public class WebCmsComponentAdminResources extends SimpleWebResourcePackage
{
	public static final String NAME = "wcm-components-admin";

	public WebCmsComponentAdminResources( WebResourcePackageManager adminWebResourcePackageManager ) {
		adminWebResourcePackageManager.register( NAME, this );

		setWebResources(
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, "jquery-ui", "https://code.jquery.com/ui/1.12.1/jquery-ui.min.js", WebResource.EXTERNAL ),
				new WebResource(
						WebResource.JAVASCRIPT_PAGE_END,
						"bootbox",
						"https://cdnjs.cloudflare.com/ajax/libs/bootbox.js/4.4.0/bootbox.min.js",
						WebResource.EXTERNAL
				),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME, "/static/WebCmsModule/js/wcm-admin-components.js", WebResource.VIEWS ),
				new WebResource( WebResource.CSS, NAME, "/static/WebCmsModule/css/wcm-admin-styles.css", WebResource.VIEWS )
		);
	}
}
