package com.foreach.imageserver.core;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;

import java.util.Set;

public class ImageServerCoreModule extends AcrossModule {

    public ImageServerCoreModule() {
        setExposeFilter(new AnnotationBeanFilter( true, Exposed.class ));
    }

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
