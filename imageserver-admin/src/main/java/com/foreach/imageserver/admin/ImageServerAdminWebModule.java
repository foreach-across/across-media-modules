package com.foreach.imageserver.admin;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.imageserver.admin.config.WebConfiguration;
import com.foreach.imageserver.admin.installers.ImageServerPermissionsInstaller;

import java.util.Set;

@AcrossDepends(required = { AdminWebModule.NAME, UserModule.NAME })
public class ImageServerAdminWebModule extends AcrossModule
{
	public static final String NAME = "ImageServerAdminWebModule";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Administrative web interface for ImageServer.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( WebConfiguration.class ) );
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				new ImageServerPermissionsInstaller()
		};
	}
}
