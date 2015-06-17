package com.foreach.imageserver.core;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.database.HasSchemaConfiguration;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.core.installers.AcrossSequencesInstaller;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.provider.*;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.imageserver.core.config.*;
import com.foreach.imageserver.core.config.conditional.ImageMagickTransformerConfiguration;
import com.foreach.imageserver.core.config.conditional.LocalImageServerClientConfiguration;
import com.foreach.imageserver.core.installers.DefaultDataInstaller;
import com.foreach.imageserver.core.installers.Image404Installer;
import com.foreach.imageserver.core.installers.InitialSchemaInstaller;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@AcrossDepends(required = { AcrossWebModule.NAME, AcrossHibernateModule.NAME })
public class ImageServerCoreModule extends AcrossModule implements HibernatePackageConfiguringModule, HasSchemaConfiguration
{
	public static final String NAME = "ImageServerCoreModule";
	private final SchemaConfiguration schemaConfiguration = new ImageSchemaConfiguration();

	@SuppressWarnings("unchecked")
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
		contextConfigurers.add(
				new AnnotatedClassConfigurer(
						ServicesConfiguration.class,
						RepositoriesConfiguration.class,
						ImageMagickTransformerConfiguration.class,
						WebConfiguration.class,
						LocalImageServerClientConfiguration.class
				)
		);
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				DefaultDataInstaller.class,
				AcrossSequencesInstaller.class,
				InitialSchemaInstaller.class,
				Image404Installer.class
		};
	}

	/**
	 * Configures the package provider associated with this implementation.
	 *
	 * @param hibernatePackage HibernatePackageRegistry.
	 */
	@Override
	public void configureHibernatePackage( HibernatePackageRegistry hibernatePackage ) {
		if ( StringUtils.equals( AcrossHibernateModule.NAME, hibernatePackage.getName() ) ) {
			hibernatePackage.add( new HibernatePackageProviderComposite(
					new PackagesToScanProvider( "com.foreach.imageserver.core.business" ),
					new TableAliasProvider( schemaConfiguration.getTables() ) ) );
		}
	}

	@Override
	public SchemaConfiguration getSchemaConfiguration() {
		return schemaConfiguration;
	}
}
