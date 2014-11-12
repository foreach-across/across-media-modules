package com.foreach.across.modules.spring.batch.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Installs the Spring batch database schema", version = 1)
public class SpringBatchSchemaInstaller extends AcrossLiquibaseInstaller
{
}
