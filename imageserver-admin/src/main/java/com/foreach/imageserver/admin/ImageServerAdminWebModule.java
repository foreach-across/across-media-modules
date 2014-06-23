package com.foreach.imageserver.admin;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.imageserver.admin.installers.ImageServerPermissionsInstaller;

@AcrossDepends(required = {AdminWebModule.NAME, UserModule.NAME})
public class ImageServerAdminWebModule extends AcrossModule {
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
    public Object[] getInstallers() {
        return new Object[]{
                new ImageServerPermissionsInstaller()
        };
    }
}
