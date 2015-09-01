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
 * Service interface for the factory responsible for creating FileRepository instances
 * if they do not yet exist.
 *
 * @see com.foreach.across.modules.filemanager.services.FileRepositoryRegistry
 */
public interface FileRepositoryFactory
{
	/**
	 * Attempt to create a new FileRepository instance with the given repository id.
	 * If creation is not possible, null should be returned.
	 *
	 * @param repositoryId Unique id of the FileRepository.
	 * @return FileRepository instance or null.
	 */
	FileRepository create( String repositoryId );
}
