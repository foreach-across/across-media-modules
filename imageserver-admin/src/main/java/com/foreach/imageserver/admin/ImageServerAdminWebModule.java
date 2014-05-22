package com.foreach.imageserver.admin;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;

@AcrossDepends(required = "AdminWebModule")
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
}
