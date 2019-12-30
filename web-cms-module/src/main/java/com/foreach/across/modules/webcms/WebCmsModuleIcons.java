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

package com.foreach.across.modules.webcms;

import com.foreach.across.modules.bootstrapui.elements.icons.IconSet;
import com.foreach.across.modules.bootstrapui.elements.icons.IconSetRegistry;
import com.foreach.across.modules.bootstrapui.elements.icons.SimpleIconSet;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.webcms.icons.WebCmsComponentIcons;
import com.foreach.across.modules.webcms.icons.WebCmsImageIcons;
import com.foreach.across.modules.webcms.icons.WebCmsMenuIcons;

import static com.foreach.across.modules.bootstrapui.BootstrapUiModuleIcons.ICON_SET_FONT_AWESOME_REGULAR;
import static com.foreach.across.modules.bootstrapui.BootstrapUiModuleIcons.ICON_SET_FONT_AWESOME_SOLID;
import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyles.css;

public class WebCmsModuleIcons
{
	public static final String ICON_SET = WebCmsModule.NAME;

	public static final WebCmsModuleIcons webCmsIcons = new WebCmsModuleIcons();

	public final static String PREVIEW = "preview";

	public WebCmsImageIcons image = new WebCmsImageIcons();

	public WebCmsMenuIcons menu = new WebCmsMenuIcons();

	public WebCmsComponentIcons component = new WebCmsComponentIcons();

	public HtmlViewElement preview() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( PREVIEW );
	}

	public static void registerIconSet() {
		SimpleIconSet mutableIconSet = new SimpleIconSet();

		mutableIconSet.add( WebCmsComponentIcons.SEARCH, ( imageName ) -> IconSet.iconSet( ICON_SET_FONT_AWESOME_SOLID ).icon( "search" ) );
		mutableIconSet.add( WebCmsComponentIcons.VIEW, ( imageName ) -> IconSet.iconSet( ICON_SET_FONT_AWESOME_SOLID ).icon( "forward" ) );

		mutableIconSet.add( WebCmsImageIcons.EDIT, ( imageName ) -> IconSet.iconSet( ICON_SET_FONT_AWESOME_SOLID ).icon( "edit" ) );
		mutableIconSet.add( WebCmsImageIcons.REMOVE, ( imageName ) -> IconSet.iconSet( ICON_SET_FONT_AWESOME_SOLID ).icon( "times" ).set( css.text.danger ) );

		mutableIconSet.add( WebCmsMenuIcons.DOMAIN_GROUP, ( imageName ) -> IconSet.iconSet( ICON_SET_FONT_AWESOME_SOLID ).icon( "home" ) );
		mutableIconSet.add( WebCmsMenuIcons.SELECTED_DOMAIN, ( imageName ) -> IconSet.iconSet( ICON_SET_FONT_AWESOME_REGULAR ).icon( "dot-circle" ) );

		mutableIconSet.add( PREVIEW, ( imageName ) -> IconSet.iconSet( ICON_SET_FONT_AWESOME_REGULAR ).icon( "file-image" ) );

		IconSetRegistry.addIconSet( ICON_SET, mutableIconSet );
	}
}
