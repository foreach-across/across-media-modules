package com.foreach.across.modules.filemanager;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;

public class FileManagerModuleSettings extends AcrossModuleSettings
{
	public static final String LOCAL_REPOSITORIES_ROOT = "fileManagerModule.localRepositoriesRoot";
	public static final String DEFAULT_TEMP_FOLDER = "fileManagerModule.tempFolder";

	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {
		registry.register( DEFAULT_TEMP_FOLDER, String.class, System.getProperty( "java.io.tmpdir" ),
		                   "Default directory for temporary files." );
		registry.register( LOCAL_REPOSITORIES_ROOT, String.class, null,
		                   "Root directory in which local repositories will be created." );
	}

	public String getDefaultTempDirectory() {
		return getProperty( DEFAULT_TEMP_FOLDER );
	}

	public String getLocalRepositoriesRoot() {
		return getProperty( LOCAL_REPOSITORIES_ROOT );
	}
}
