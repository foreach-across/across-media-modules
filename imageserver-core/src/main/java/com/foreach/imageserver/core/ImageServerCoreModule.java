package com.foreach.imageserver.core;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.imageserver.core.config.*;
import com.foreach.imageserver.core.installers.Image404Installer;
import com.foreach.imageserver.core.installers.InitialSchemaInstaller;

import java.util.Set;

@AcrossDepends(required = { AcrossWebModule.NAME, AcrossHibernateModule.NAME })
public class ImageServerCoreModule extends AcrossModule
{
	public static final String NAME = "ImageServerCoreModule";

	public ImageServerCoreModule() {
		setExposeFilter( new AnnotationBeanFilter( true, Exposed.class ) );
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "ImageServer core module";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( ImageServerCoreConfig.class, ServicesConfiguration.class,
		                                                      ImageMagickTransformerConfiguration.class,
		                                                      ControllersConfiguration.class,
		                                                      MultipartResolverConfiguration.class ) );
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] { InitialSchemaInstaller.class, Image404Installer.class };
	}
}
