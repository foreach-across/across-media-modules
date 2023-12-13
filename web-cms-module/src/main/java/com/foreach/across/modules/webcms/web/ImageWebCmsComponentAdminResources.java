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

import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackage;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.web.resource.WebResource.*;
import static com.foreach.across.modules.web.resource.WebResourceRule.add;
import static com.foreach.across.modules.web.resource.WebResourceRule.addPackage;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
public class ImageWebCmsComponentAdminResources implements WebResourcePackage
{
	public static final String NAME = "wcm-image-components-admin";

	public ImageWebCmsComponentAdminResources( WebResourcePackageManager adminWebResourcePackageManager ) {
		adminWebResourcePackageManager.register( NAME, this );
	}

	@Override
	public void install( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.apply(
				addPackage( WebCmsComponentAdminResources.NAME ),

				// WebCmsModule specific
				add( WebResource.css( "@static:/WebCmsModule/css/wcm-admin-image-component-styles.css" ) ).withKey( NAME ).toBucket( CSS ),
				add( WebResource.javascript( "@static:/WebCmsModule/js/wcm-admin-image-components.js" ) ).withKey( NAME ).toBucket( JAVASCRIPT_PAGE_END ),

				// Ensure EntityQuery filtering is possible in dialog
				add( WebResource.javascript( "@static:/entity/js/entity-query.js" ) ).withKey( "entityQueryFilterForm" ).toBucket( JAVASCRIPT_PAGE_END ),

				// Lodash
				add( WebResource.javascript( "@webjars:/lodash/4.17.4/lodash.min.js" ) )
						.withKey( "lodash" )
						.toBucket( JAVASCRIPT )
		);
	}
}
