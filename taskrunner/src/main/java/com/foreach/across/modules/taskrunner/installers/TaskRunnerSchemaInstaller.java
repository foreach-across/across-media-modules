package com.foreach.across.modules.taskrunner.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Installs the taskrunner database schema", version = 1)
public class TaskRunnerSchemaInstaller extends AcrossLiquibaseInstaller
{
	public TaskRunnerSchemaInstaller( SchemaConfiguration schemaConfiguration ) {
		super( schemaConfiguration );
	}
}
