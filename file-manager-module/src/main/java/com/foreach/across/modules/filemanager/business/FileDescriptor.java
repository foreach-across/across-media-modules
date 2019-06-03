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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Represents a single {@link FileResource} in a specific FileRepository.
 */
@EqualsAndHashCode
public class FileDescriptor implements FileRepositoryResourceDescriptor
{
	private static final long serialVersionUID = 1L;

	/**
	 * Unique id of the file within the folder.
	 */
	@Getter
	private final String fileId;

	/**
	 * The descriptor of the folder this file would belong to.
	 */
	@Getter
	private final FolderDescriptor folderDescriptor;

	/**
	 * @deprecated in favour of {@link FileDescriptor#of(String)}. Will be removed in 2.0.0.
	 */
	@Deprecated
	public FileDescriptor( @NonNull String uri ) {
		// todo: make private in 2.0.0
		if ( StringUtils.isBlank( uri ) ) {
			throw new IllegalArgumentException( "uri may not be null or empty" );
		}

		String[] parts = StringUtils.split( StringUtils.removeStart( uri, "axfs://" ), ":" );

		Assert.isTrue( parts.length == 2 || parts.length == 3, "FileDescriptor URI must contain either 2 or 3 segments separated with :" );

		String folderId;

		if ( parts.length == 2 ) {
			String cleaned = StringUtils.replace( parts[1], "\\", "/" );
			int lastSeparator = StringUtils.lastIndexOf( cleaned, "/" );
			folderId = lastSeparator >= 0 ? cleaned.substring( 0, lastSeparator ) : null;
			fileId = lastSeparator >= 0 ? cleaned.substring( lastSeparator + 1 ) : cleaned;
		}
		else {
			folderId = parts[1];
			fileId = parts[2];
		}

		folderDescriptor = FolderDescriptor.of( parts[0], folderId );
	}

	/**
	 * @deprecated in favour of {@link FileDescriptor#of(String, String)}. Will be removed in 2.0.0.
	 */
	@Deprecated
	public FileDescriptor( String repositoryId, String fileId ) {
		this( repositoryId, null, fileId );
	}

	/**
	 * @deprecated in favour of {@link FileDescriptor#of(String, String, String)}. Will be removed in 2.0.0.
	 */
	@Deprecated
	public FileDescriptor( String repositoryId, String folderId, @NonNull String fileId ) {
		folderDescriptor = FolderDescriptor.of( repositoryId, folderId );
		this.fileId = fileId;
	}

	/**
	 * @return Unique uri of the entire file.
	 */
	@Override
	public String getUri() {
		StringBuilder uri = new StringBuilder( folderDescriptor.getRepositoryId() ).append( ":" );

		if ( folderDescriptor.getFolderId() != null ) {
			uri.append( folderDescriptor.getFolderId() ).append( ":" );
		}

		uri.append( fileId );

		return uri.toString();
	}

	/**
	 * @return Unique id of the repository this file belongs to.
	 */
	@Override
	public String getRepositoryId() {
		return folderDescriptor.getRepositoryId();
	}

	/**
	 * @return Path within the repository this file can be found, null if the file is considered in the root folder.
	 */
	public String getFolderId() {
		return folderDescriptor.getFolderId();
	}

	@Override
	public String toString() {
		return getUri();
	}

	/**
	 * Creates a {@link FileDescriptor} based on a uri.
	 *
	 * @param uri of the file
	 * @return the descriptor
	 */
	@SuppressWarnings("deprecation")
	public static FileDescriptor of( @NonNull String uri ) {
		return new FileDescriptor( uri );
	}

	/**
	 * Creates a {@link FileDescriptor} in the root folder of a specific repository.
	 *
	 * @param repositoryId in which the file should be stored
	 * @param fileId       identifier of the file
	 * @return the descriptor
	 */
	public static FileDescriptor of( String repositoryId, String fileId ) {
		return FileDescriptor.of( repositoryId, null, fileId );
	}

	/**
	 * Creates a {@link FileDescriptor} for a specific folder in a repository.
	 *
	 * @param repositoryId in which the file should be stored
	 * @param folderId     in the repository
	 * @param fileId       identifier of the file
	 * @return the descriptor
	 */
	@SuppressWarnings("all")
	public static FileDescriptor of( String repositoryId, String folderId, String fileId ) {
		return new FileDescriptor( repositoryId, folderId, fileId );
	}
}
