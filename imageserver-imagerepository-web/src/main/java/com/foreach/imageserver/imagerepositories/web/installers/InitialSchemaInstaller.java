package com.foreach.imageserver.imagerepositories.web.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Creates ImageServer web ImageRepository initial schema.", version = 1)
public class InitialSchemaInstaller extends AcrossLiquibaseInstaller {
    public InitialSchemaInstaller() {
        super("classpath:com/foreach/imageserver/imagerepositories/web/liquibase/changelog.xml");
    }
}
