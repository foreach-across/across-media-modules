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

package com.foreach.across.modules.filemanager.business;

import org.springframework.core.io.WritableResource;

/**
 * Represents a single {@link com.foreach.across.modules.filemanager.services.FileRepository} file,
 * identified by a {@link FileDescriptor}. Can be used to read or manipulate the file (including update and delete).
 */
public interface FileResource extends WritableResource
{
	/*OutputStream getOutputStream();

	void copyFrom( File file );

	void copyFrom( InputStream inputStream );

	File getAsFile();

	boolean exists();

	boolean delete();
	*/

	/**
	 * @return the descriptor to this file resource
	 */
	FileDescriptor getFileDescriptor();

	/**
	 * Delete the actual file. The return code should give an indication if
	 * delete has failed or not. Whereas {@code true} might not guarantee that a
	 * file has been deleted, {@code false} should guarantee that delete has failed.
	 * <p/>
	 * If delete was successful, subsequent calls to {@link #exists()} should return {@code false}.
	 *
	 * @return false if delete failed
	 */
	boolean delete();

	/**
	 * @return the descriptor
	 */
//	default FolderDescriptor getFolderDescriptor() {
//		return getFileDescriptor().getFolderId();
//	}
	@Override
	FileResource createRelative( String relativePath );
}
