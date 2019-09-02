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

package com.foreach.across.modules.webcms.config;

import com.foreach.across.modules.bootstrapui.elements.icons.IconSet;
import com.foreach.across.modules.bootstrapui.elements.icons.IconSetRegistry;
import com.foreach.across.modules.bootstrapui.elements.icons.SimpleIconSet;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.webcms.WebCmsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.foreach.across.modules.bootstrapui.config.FontAwesomeIconSetConfiguration.FONT_AWESOME_SOLID_ICON_SET;
import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyles.css;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

@Configuration
public class WebCmsIcons
{
	public static final WebCmsIcons webCmsIcons = new WebCmsIcons();

	public AbstractNodeViewElement search() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( "search" );
	}

	public AbstractNodeViewElement edit() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( "edit" );
	}

	public AbstractNodeViewElement remove() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( "remove" );
	}

	public AbstractNodeViewElement home() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( "home" );
	}

	public AbstractNodeViewElement preview() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( "preview" );
	}

	public AbstractNodeViewElement viewComponents() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( "viewComponents" );
	}

	public AbstractNodeViewElement selectedItem() {
		return IconSetRegistry.getIconSet( WebCmsModule.NAME ).icon( "selected" );
	}

	@Autowired
	public void registerIconSet() {
		SimpleIconSet mutableIconSet = new SimpleIconSet();

		mutableIconSet.add( "search", ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "search" ) );
		mutableIconSet.add( "edit", ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "edit" ) );
		mutableIconSet.add( "remove", ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "times" ).set( css.text.danger ) );
		mutableIconSet.add( "home", ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "home" ) );
		mutableIconSet.add( "preview", ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "file-image" ) );
		mutableIconSet.add( "viewComponents", ( imageName ) -> IconSet.iconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "forward" ) );

		SimpleIconSet regulars = new SimpleIconSet();
		regulars.setDefaultIconResolver( ( iconName ) -> html.i( HtmlViewElement.Functions.css( "far fa-" + iconName ) ) );
		IconSetRegistry.addIconSet( "fontawesome-regular", regulars );
		mutableIconSet.add( "selected", ( imageName ) -> IconSet.iconSet( "fontawesome-regular" ).icon( "dot-circle" ) );

		IconSetRegistry.addIconSet( WebCmsModule.NAME, mutableIconSet );
	}
}
