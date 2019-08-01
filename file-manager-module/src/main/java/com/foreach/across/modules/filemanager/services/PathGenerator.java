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

package com.foreach.across.modules.filemanager.services;

/**
 * PathGenerator interface can be used to dynamically generate a path
 * to distribute files in a repository.
 *
 * @see com.foreach.across.modules.filemanager.services.LocalFileRepository
 * @see com.foreach.across.modules.filemanager.services.DateFormatPathGenerator
 */
@FunctionalInterface
public interface PathGenerator
{
	/**
	 * Generate a path for a new file.
	 *
	 * @return Path string or null if no sub folder should be used.
	 */
	String generatePath();
}
