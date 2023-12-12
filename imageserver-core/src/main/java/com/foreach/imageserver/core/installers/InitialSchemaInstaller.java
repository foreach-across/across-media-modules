package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;

@Installer(description = "Creates ImageServer core initial schema.", version = 11)
public class InitialSchemaInstaller extends AcrossLiquibaseInstaller
{
	public InitialSchemaInstaller() {
		super( "com/foreach/imageserver/core/installers/InitialSchemaInstaller.xml" );
	}
}
