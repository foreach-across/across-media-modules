package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Creates ImageServer core initial schema.", version = 11)
public class InitialSchemaInstaller extends AcrossLiquibaseInstaller
{
}
