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

import com.foreach.across.modules.bootstrapui.resource.BootstrapUiWebResources;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackage;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.web.resource.WebResource.CSS;
import static com.foreach.across.modules.web.resource.WebResource.JAVASCRIPT_PAGE_END;
import static com.foreach.across.modules.web.resource.WebResourceRule.add;

/**
 * Contains the general web resources to enable the administration UI for {@link WebCmsComponentModel}s.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
public class WebCmsComponentAdminResources implements WebResourcePackage
{
	public static final String NAME = "wcm-components-admin";

	public WebCmsComponentAdminResources( WebResourcePackageManager adminWebResourcePackageManager ) {
		adminWebResourcePackageManager.register( NAME, this );
	}

	@Override
	public void install( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.apply(
				add( WebResource.javascript( "@webjars:/jquery-ui/1.12.1/jquery-ui.min.js" ) )
						.withKey( "jquery-ui" )
						.toBucket( JAVASCRIPT_PAGE_END )
						.before( BootstrapUiWebResources.NAME ),
				add( WebResource.javascript( "@webjars:/bootbox.js/5.3.2/bootbox.js" ) )
						.withKey( "bootbox" )
						.toBucket( JAVASCRIPT_PAGE_END ),
				add( WebResource.javascript( "@static:/WebCmsModule/js/wcm-admin-components.js" ) )
						.withKey( NAME )
						.toBucket( JAVASCRIPT_PAGE_END ),
				add( WebResource.css( "@static:/WebCmsModule/css/wcm-admin-styles.css" ) )
						.withKey( NAME )
						.toBucket( CSS )
		);
	}
}
