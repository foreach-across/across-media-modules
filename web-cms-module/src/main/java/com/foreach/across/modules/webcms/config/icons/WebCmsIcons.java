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

package com.foreach.across.modules.webcms.config.icons;

import com.foreach.across.modules.bootstrapui.elements.icons.IconSet;
import com.foreach.across.modules.bootstrapui.elements.icons.IconSetRegistry;
import com.foreach.across.modules.bootstrapui.elements.icons.SimpleIconSet;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.webcms.WebCmsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.foreach.across.modules.bootstrapui.config.FontAwesomeIconSetConfiguration.FONT_AWESOME_REGULAR_ICON_SET;
import static com.foreach.across.modules.bootstrapui.config.FontAwesomeIconSetConfiguration.FONT_AWESOME_SOLID_ICON_SET;
import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyles.css;

@Configuration
public class WebCmsIcons
{
	public static final WebCmsIcons webCmsIcons = new WebCmsIcons();

	public final static String PREVIEW = "preview";

	public WebCmsImageIcons image = new WebCmsImageIcons();

	public WebCmsMenuIcons menu = new WebCmsMenuIcons();

	public WebCmsComponentIcons component = new WebCmsComponentIcons();

	public AbstractNodeViewElement preview() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( PREVIEW );
	}

	@Autowired
	public void registerIconSet() {
		SimpleIconSet mutableIconSet = new SimpleIconSet();

		mutableIconSet.add( WebCmsComponentIcons.SEARCH, ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "search" ) );
		mutableIconSet.add( WebCmsComponentIcons.VIEW, ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "forward" ) );

		mutableIconSet.add( WebCmsImageIcons.EDIT, ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "edit" ) );
		mutableIconSet.add( WebCmsImageIcons.REMOVE, ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "times" ).set( css.text.danger ) );

		mutableIconSet.add( WebCmsMenuIcons.DOMAIN_GROUP, ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "home" ) );
		mutableIconSet.add( WebCmsMenuIcons.SELECTED_DOMAIN, ( imageName ) -> IconSet.iconSet( FONT_AWESOME_REGULAR_ICON_SET ).icon( "dot-circle" ) );

		mutableIconSet.add( PREVIEW, ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "file-image" ) );

		IconSetRegistry.addIconSet( WebCmsModule.NAME, mutableIconSet );
	}
}
