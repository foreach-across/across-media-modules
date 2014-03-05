package com.foreach.imageserver.connectors.dpp;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;

import java.util.Set;

public class DioContentModule extends AcrossModule {

    @Override
    public String getName() {
        return "DioContentModule";
    }

    @Override
    public String getDescription() {
        return "Provides a connector for images stored in dio content";
    }

    @Override
    protected void registerDefaultApplicationContextConfigurers(Set<ApplicationContextConfigurer> contextConfigurers) {
        contextConfigurers.add(new AnnotatedClassConfigurer(DioContentConfig.class));
    }
}
