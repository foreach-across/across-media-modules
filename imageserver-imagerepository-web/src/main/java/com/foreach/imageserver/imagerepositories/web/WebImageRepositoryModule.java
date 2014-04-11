package com.foreach.imageserver.imagerepositories.web;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.imageserver.imagerepositories.web.installers.InitialSchemaInstaller;

import java.util.Set;

@AcrossDepends(required = {"ImageServerCoreModule"})
public class WebImageRepositoryModule extends AcrossModule {

    public WebImageRepositoryModule() {
        setExposeFilter(new AnnotationBeanFilter(true, Exposed.class));
    }

    @Override
    public String getName() {
        return "ImageServerWebImageRepository";
    }

    @Override
    public String getDescription() {
        return "Mediafin Image Server Web Image Repository Module";
    }

    @Override
    protected void registerDefaultApplicationContextConfigurers(Set<ApplicationContextConfigurer> contextConfigurers) {
        contextConfigurers.add(new AnnotatedClassConfigurer(WebImageRepositoryConfig.class));
    }

    @Override
    public Object[] getInstallers() {
        return new Object[]{new InitialSchemaInstaller()};
    }
}