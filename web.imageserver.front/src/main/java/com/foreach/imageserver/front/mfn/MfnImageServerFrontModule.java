package com.foreach.imageserver.front.mfn;

import com.foreach.imageserver.front.mfn.installers.MfnImageResolutionsInstaller;
import com.foreach.imageserver.front.mfn.installers.MfnImageServerMigrationResolutionsInstaller;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;

import java.util.Set;

@AcrossDepends(required = "ImageServerCoreModule")
public class MfnImageServerFrontModule extends AcrossModule {

    public MfnImageServerFrontModule() {
        setExposeFilter(new AnnotationBeanFilter(true, Exposed.class));
    }

    @Override
    public String getName() {
        return "MfnImageServerFrontModule";
    }

    @Override
    public String getDescription() {
        return "MFN front module for Foreach image server";
    }

    @Override
    protected void registerDefaultApplicationContextConfigurers(Set<ApplicationContextConfigurer> contextConfigurers) {
    }

    @Override
    public Object[] getInstallers() {
        return new Object[]{new MfnImageResolutionsInstaller(), new MfnImageServerMigrationResolutionsInstaller()};
    }
}
