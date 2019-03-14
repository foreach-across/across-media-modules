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

import static com.foreach.across.modules.web.resource.WebResource.CSS;
import static com.foreach.across.modules.web.resource.WebResource.JAVASCRIPT_PAGE_END;
import static com.foreach.across.modules.web.resource.WebResourceRule.add;
import static com.foreach.across.modules.web.resource.WebResourceRule.addPackage;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
public class TextWebCmsComponentAdminResources implements WebResourcePackage
{
	public static final String NAME = "wcm-text-components-admin";

	public TextWebCmsComponentAdminResources( WebResourcePackageManager adminWebResourcePackageManager ) {
		adminWebResourcePackageManager.register( NAME, this );
	}

	@Override
	public void install( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.apply(
				addPackage( ImageWebCmsComponentAdminResources.NAME ),

				// TinyMCE
				add( WebResource.javascript( "@static:/WebCmsModule/js/tinymce/tinymce.min.js" ) )
						.withKey( "tinymce" )
						.toBucket( JAVASCRIPT_PAGE_END ),
				add( WebResource.javascript( "@static:/WebCmsModule/js/jquery.sticky.js" ) )
						.withKey( "sticky" )
						.toBucket( JAVASCRIPT_PAGE_END ),

				// CodeMirror
				add( WebResource.css( "@webjars:/codemirror-minified/5.44.0/lib/codemirror.css" ) )
		                   .withKey( "codemirror-css" )
		                   .toBucket( CSS ),
				add( WebResource.javascript( "@webjars:/codemirror-minified/5.44.0/lib/codemirror.js" ) )
		                   .withKey( "codemirror" )
		                   .toBucket( JAVASCRIPT_PAGE_END ),
				add( WebResource.javascript( "@webjars:/codemirror-minified/5.44.0/mode/htmlmixed/htmlmixed.js" ) )
		                   .withKey( "codemirror-htmlmixed" )
		                   .toBucket( JAVASCRIPT_PAGE_END ),
				add( WebResource.javascript( "@webjars:/codemirror-minified/5.44.0/mode/xml/xml.js" ) )
		                   .withKey( "codemirror-xml" )
		                   .toBucket( JAVASCRIPT_PAGE_END ),

				// WebCmsModule specific
				add( WebResource.css( "@static:/WebCmsModule/css/wcm-admin-text-component-styles.css" ) ).withKey( NAME ).toBucket( CSS ),
				add( WebResource.javascript( "@static:/WebCmsModule/js/wcm-admin-text-components.js" ) ).withKey( NAME ).toBucket( JAVASCRIPT_PAGE_END )
		);
	}
}
