/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
