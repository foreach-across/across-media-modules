package com.foreach.across.modules.filemanager.config;

import com.foreach.across.modules.bootstrapui.elements.icons.IconSetRegistry;
import com.foreach.across.modules.bootstrapui.elements.icons.SimpleIconSet;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.foreach.across.modules.bootstrapui.config.FontAwesomeIconSetConfiguration.FONT_AWESOME_SOLID_ICON_SET;
import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyles.css;

@Configuration
public class FileManagerIcons
{
	public final static String REMOVE_FILE = "remove-file";

	public static final FileManagerIcons fileManagerIcons = new FileManagerIcons();

	public AbstractNodeViewElement removeFile() {
		return IconSetRegistry.getIconSet( FileManagerModule.NAME ).icon( REMOVE_FILE );
	}

	@Autowired
	public void registerIconset() {
		SimpleIconSet iconset = new SimpleIconSet();

		iconset.add( REMOVE_FILE, ( name ) -> IconSetRegistry.getIconSet( FONT_AWESOME_SOLID_ICON_SET ).icon( "times" ).set( css.text.danger ) );

		IconSetRegistry.addIconSet( FileManagerModule.NAME, iconset );
	}
}
