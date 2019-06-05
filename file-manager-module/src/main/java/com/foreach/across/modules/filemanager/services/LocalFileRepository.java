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

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The simplest of file repositories, storing all files in a set of local folders
 * with one root parent folder.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.filemanager.services.PathGenerator
 * @see #builder()
 * @since 1.0.0
 */
@Slf4j
public class LocalFileRepository extends AbstractFileRepository
{
	@Getter
	private String rootFolderPath;

	/**
	 * @deprecated since 1.4.0 - use {@link #builder()} instead
	 */
	@Deprecated
	@SuppressWarnings("unused")
	public LocalFileRepository( String repositoryId, String rootFolderPath ) {
		this( repositoryId, rootFolderPath, null );
	}

	@Builder
	private LocalFileRepository( String repositoryId, @NonNull String rootFolder, PathGenerator pathGenerator ) {
		super( repositoryId );
		this.rootFolderPath = rootFolder;
		setPathGenerator( pathGenerator );
	}

	@Override
	protected FileResource buildFileResource( FileDescriptor descriptor ) {
		return new LocalFileResource( descriptor, buildPath( descriptor ) );
	}

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		return new LocalFolderResource( descriptor, Paths.get( rootFolderPath, StringUtils.defaultString( descriptor.getFolderId() ) ) );
	}

	private Path buildPath( FileDescriptor descriptor ) {
		if ( descriptor.getFolderId() != null ) {
			return Paths.get( rootFolderPath, descriptor.getFolderId(), descriptor.getFileId() );
		}

		return Paths.get( rootFolderPath, descriptor.getFileId() );
	}
}
