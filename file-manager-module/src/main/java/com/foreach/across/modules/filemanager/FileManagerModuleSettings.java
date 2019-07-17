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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@EqualsAndHashCode
@ConfigurationProperties("file-manager-module")
public class FileManagerModuleSettings
{
	public static final String LOCAL_REPOSITORIES_ROOT = "file-manager-module.local-repositories-root";
	public static final String DEFAULT_TEMP_FOLDER = "file-manager-module.temp-folder";

	/***
	 * The location of the root directory in which local repositories will be created.
	 */
	private String localRepositoriesRoot;

	/***
	 * The location of default directory for temporary files.
	 */
	private String tempFolder = System.getProperty( "java.io.tmpdir" );

	/**
	 * Expiration task configuration properties.
	 */
	private ExpirationProperties expiration = new ExpirationProperties();

	@Getter
	@Setter
	@EqualsAndHashCode
	public static class ExpirationProperties
	{
		/**
		 * Should the expiration task be enabled (automatic expiry of tracked items in AbstractExpiringFileRepository implementations found.
		 */
		private boolean enabled;

		/**
		 * Number of seconds delay between expiration task executions.
		 */
		private int intervalSeconds = 300;
	}
}
