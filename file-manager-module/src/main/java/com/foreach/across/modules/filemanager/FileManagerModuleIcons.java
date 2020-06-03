package com.foreach.across.modules.filemanager;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.bootstrapui.elements.icons.IconSetRegistry;
import com.foreach.across.modules.bootstrapui.elements.icons.SimpleIconSet;
import com.foreach.across.modules.bootstrapui.styles.AcrossBootstrapStyles;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import org.springframework.context.annotation.Configuration;

import static com.foreach.across.modules.bootstrapui.BootstrapUiModuleIcons.ICON_SET_FONT_AWESOME_SOLID;

@Configuration
@ConditionalOnAcrossModule(BootstrapUiModule.NAME)
public class FileManagerModuleIcons
{
	public static final String ICON_SET = FileManagerModule.NAME;

	public static final String REMOVE_FILE = "remove-file";

	public static final FileManagerModuleIcons fileManagerIcons = new FileManagerModuleIcons();

	public HtmlViewElement removeFile() {
		return IconSetRegistry.getIconSet( FileManagerModule.NAME ).icon( REMOVE_FILE );
	}

	public static void registerIconSet() {
		SimpleIconSet iconset = new SimpleIconSet();
		iconset.add( REMOVE_FILE, ( name ) -> IconSetRegistry.getIconSet( ICON_SET_FONT_AWESOME_SOLID ).icon( "times" )
		                                                     .set( AcrossBootstrapStyles.css.text.danger ) );
		IconSetRegistry.addIconSet( ICON_SET, iconset );
	}
}
