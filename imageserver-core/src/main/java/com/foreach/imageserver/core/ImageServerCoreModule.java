package com.foreach.imageserver.core;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

import java.util.Set;

public class ImageServerCoreModule extends AcrossModule {

    @Override
    public String getName() {
        return "ImageServerCore";
    }

    @Override
    public String getDescription() {
        return "Foreach Image Server Core Module";
    }

    @Override
    protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
        contextConfigurers.add( new AnnotatedClassConfigurer( ImageServerCoreConfig.class ) );
    }

}
