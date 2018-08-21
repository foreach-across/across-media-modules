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

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * Represents a single file in a FileRepository.
 */
public class FileDescriptor
{
	private final String repositoryId, fileId, folderId;

	private final String uri;

	public FileDescriptor( String uri ) {
		this.uri = uri;

		String[] parts = StringUtils.split( uri, ":" );

		Assert.isTrue( parts.length == 2 || parts.length == 3 );

		this.repositoryId = parts[0];

		if ( parts.length == 2 ) {
			this.folderId = null;
			this.fileId = parts[1];
		}
		else {
			this.folderId = parts[1];
			this.fileId = parts[2];
		}
	}

	public FileDescriptor( String repositoryId, String fileId ) {
		this( repositoryId, null, fileId );
	}

	public FileDescriptor( String repositoryId, String folderId, String fileId ) {
		this.repositoryId = repositoryId;
		this.fileId = fileId;
		this.folderId = folderId;

		uri = buildUri( repositoryId, folderId, fileId );
	}

	/**
	 * @return Unique uri of the entire file.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return Unique id of the repository this file belongs to.
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * @return Unique id of the file within the folder.
	 */
	public String getFileId() {
		return fileId;
	}

	/**
	 * @return Path within the repository this file can be found, null if the file is considered in the root folder.
	 */
	public String getFolderId() {
		return folderId;
	}

	@Override
	public String toString() {
		return uri;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		FileDescriptor that = (FileDescriptor) o;

		return Objects.equals( repositoryId, that.repositoryId )
				&& Objects.equals( folderId, that.folderId )
				&& Objects.equals( fileId, that.fileId );
	}

	@Override
	public int hashCode() {
		return Objects.hash( repositoryId, folderId, fileId );
	}

	public static String buildUri( String repositoryId, String folderId, String fileId ) {
		StringBuilder uri = new StringBuilder( repositoryId ).append( ":" );

		if ( folderId != null ) {
			uri.append( folderId ).append( ":" );
		}

		uri.append( fileId );

		return uri.toString();
	}
}
