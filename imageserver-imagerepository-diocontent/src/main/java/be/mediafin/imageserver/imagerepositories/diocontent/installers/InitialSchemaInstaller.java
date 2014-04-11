package be.mediafin.imageserver.imagerepositories.diocontent.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Creates ImageServer DioContent ImageRepository initial schema.", version = 1)
public class InitialSchemaInstaller extends AcrossLiquibaseInstaller {
    public InitialSchemaInstaller() {
        super("classpath:be/mediafin/imageserver/imagerepositories/diocontent/liquibase/changelog.xml");
    }
}
