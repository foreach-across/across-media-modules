package be.mediafin.imageserver.front.mfn.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;

@Installer(description = "Inserts MFN image resolutions in DB", version = 1)
public class MfnImageResolutionsInstaller extends AcrossLiquibaseInstaller {
}