package com.foreach.across.modules.taskrunner;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.database.HasSchemaConfiguration;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.provider.*;
import com.foreach.across.modules.taskrunner.config.TaskRunnerRepositoriesConfiguration;
import com.foreach.across.modules.taskrunner.config.TaskRunnerServicesConfiguration;
import com.foreach.across.modules.taskrunner.config.TaskRunnerSchemaConfiguration;
import com.foreach.across.modules.taskrunner.installers.TaskRunnerSchemaInstaller;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * @author Arne Vandamme
 */
@AcrossDepends(required = AcrossHibernateModule.NAME)
public class TaskRunnerModule extends AcrossModule implements HasHibernatePackageProvider, HasSchemaConfiguration
{
	public static final String NAME = "TaskRunnerModule";

	private final SchemaConfiguration schemaConfiguration = new TaskRunnerSchemaConfiguration();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Services for executing tasks (a)synchronously across multiple workers.";
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] { new TaskRunnerSchemaInstaller( schemaConfiguration ) };
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new AnnotatedClassConfigurer( TaskRunnerRepositoriesConfiguration.class,
		                                                      TaskRunnerServicesConfiguration.class ) );
	}

	/**
	 * Returns the package provider associated with this implementation.
	 *
	 * @param hibernateModule AcrossHibernateModule that is requesting packages.
	 * @return HibernatePackageProvider instance.
	 */
	public HibernatePackageProvider getHibernatePackageProvider( AcrossHibernateModule hibernateModule ) {
		if ( StringUtils.equals( "AcrossHibernateModule", hibernateModule.getName() ) ) {
			return new HibernatePackageProviderComposite(
					new PackagesToScanProvider( "com.foreach.across.modules.taskrunner.business" ),
					new TableAliasProvider( schemaConfiguration.getTables() ) );
		}

		return null;
	}

	@Override
	public SchemaConfiguration getSchemaConfiguration() {
		return schemaConfiguration;
	}
}
