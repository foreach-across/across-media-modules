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
public class TextWebCmsComponentAdminResources extends SimpleWebResourcePackage
{
	public static final String NAME = "wcm-text-components-admin";

	public TextWebCmsComponentAdminResources( WebResourcePackageManager adminWebResourcePackageManager ) {
		adminWebResourcePackageManager.register( NAME, this );

		setDependencies( ImageWebCmsComponentAdminResources.NAME );

		setWebResources(
				new WebResource( WebResource.CSS, NAME, "/static/WebCmsModule/css/wcm-admin-text-component-styles.css", WebResource.VIEWS ),

				// ckeditor
				//new WebResource( WebResource.JAVASCRIPT_PAGE_END, "ckeditor", "https://cdn.ckeditor.com/4.6.2/standard/ckeditor.js", WebResource.EXTERNAL ),

				// TinyMCE
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, "tinymce", "/static/WebCmsModule/js/tinymce/tinymce.min.js", WebResource.VIEWS ),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, "sticky", "/static/WebCmsModule/js/jquery.sticky.js", WebResource.VIEWS ),

				// codemirror
				new WebResource( WebResource.CSS, "codemirror-css", "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.25.0/codemirror.min.css",
				                 WebResource.EXTERNAL ),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, "codemirror", "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.25.0/codemirror.min.js",
				                 WebResource.EXTERNAL ),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, "codemirror-htmlmixed",
				                 "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.25.0/mode/htmlmixed/htmlmixed.min.js",
				                 WebResource.EXTERNAL ),
				new WebResource( WebResource.JAVASCRIPT_PAGE_END, "codemirror-xml",
				                 "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.25.0/mode/xml/xml.min.js",
				                 WebResource.EXTERNAL ),

				new WebResource( WebResource.JAVASCRIPT_PAGE_END, NAME, "/static/WebCmsModule/js/wcm-admin-text-components.js", WebResource.VIEWS )
		);
	}
}
