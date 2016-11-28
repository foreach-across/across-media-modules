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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fileManagerModule")
public class FileManagerModuleSettings
{
	public static final String LOCAL_REPOSITORIES_ROOT = "fileManagerModule.localRepositoriesRoot";
	public static final String DEFAULT_TEMP_FOLDER = "fileManagerModule.tempFolder";

	/***
	 * The location of the root directory in which local repositories will be created.
	 */
	private String localRepositoriesRoot;

	/***
	 * The location of default directory for temporary files.
	 */
	private String tempFolder = System.getProperty("java.io.tmpdir");

	public String getLocalRepositoriesRoot() {
		return localRepositoriesRoot;
	}

	public void setLocalRepositoriesRoot(String localRepositoriesRoot) {
		this.localRepositoriesRoot = localRepositoriesRoot;
	}

	public String getTempFolder() {
		return tempFolder;
	}

	public void setTempFolder(String tempFolder) {
		this.tempFolder = tempFolder;
	}
}
