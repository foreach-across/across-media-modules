package com.foreach.imageserver.core.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Creates ImageServer core initial schema.", version = 3)
public class InitialSchemaInstaller extends AcrossLiquibaseInstaller {
    public InitialSchemaInstaller() {
        super("classpath:com/foreach/imageserver/core/liquibase/changelog.xml");
    }
}
