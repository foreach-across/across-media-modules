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

import com.foreach.across.modules.filemanager.context.FileResourceProtocolResolver;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Represents a {@link FolderResource} in a specific {@link com.foreach.across.modules.filemanager.services.FileRepository}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("WeakerAccess")
public class FolderDescriptor implements FileRepositoryResourceDescriptor
{
	private static final long serialVersionUID = 1L;
	private static final Pattern INVALID_FOLDER_ID = Pattern.compile( "(/?\\.{1,2}(/|$))|(^\\s+$)|:" );

	/**
	 * Unique id of the repository this folder belongs to.
	 */
	@Getter
	private final String repositoryId;

	/**
	 * Path within the repository to this folder, null if the folder is considered the root folder.
	 */
	@Getter
	private final String folderId;

	/**
	 * The descriptor or the parent folder based on the folder id of the current descriptor.
	 * Will be empty if the current descriptor represents the root folder.
	 *
	 * @return parent folder descriptor
	 */
	public Optional<FolderDescriptor> getParentFolderDescriptor() {
		if ( folderId == null ) {
			return Optional.empty();
		}

		String parentFolderId = substringBeforeLast( folderId, "/" );
		return Optional.of( parentFolderId.length() == folderId.length() ? rootFolder( repositoryId ) : of( repositoryId, parentFolderId ) );
	}

	/**
	 * @return Unique uri of the folder
	 */
	@Override
	public String getUri() {
		return repositoryId + ":" + ( folderId != null ? folderId + "/" : "/" );
	}

	/**
	 * Create the folder descriptor for a child folder represented by the relative path.
	 * The path can have multiple path segments.
	 *
	 * @param relativePath to the child folder
	 * @return descriptor
	 */
	public FolderDescriptor createFolderDescriptor( @NonNull String relativePath ) {
		String p = relativePath.startsWith( "/" ) ? relativePath : "/" + relativePath;
		return of( repositoryId, StringUtils.defaultString( folderId ) + p );
	}

	/**
	 * Create the file descriptor for a file in the folder represented by the relative path.
	 * The path can have multiple path segments, all segments except the last will be considered sub-folders.
	 *
	 * @param relativePath to the child folder
	 * @return descriptor
	 */
	public FileDescriptor createFileDescriptor( @NonNull String relativePath ) {
		String p = relativePath.startsWith( "/" ) ? relativePath : "/" + relativePath;
		return FileDescriptor.of( repositoryId + ":" + ( folderId != null ? "/" + folderId : "" ) + p );
	}

	@Override
	public String toString() {
		return getUri();
	}

	/**
	 * Create a descriptor for the root folder in the specified repository.
	 *
	 * @param repositoryId representing the repository
	 * @return descriptor
	 */
	public static FolderDescriptor rootFolder( @NonNull String repositoryId ) {
		return of( repositoryId, null );
	}

	/**
	 * Creates a {@link FolderDescriptor} based on an URI string.
	 *
	 * @param uri of the folder
	 * @return descriptor
	 */
	public static FolderDescriptor of( @NonNull String uri ) {
		String[] parts = StringUtils.split( StringUtils.removeStart( uri, FileResourceProtocolResolver.PROTOCOL ), ":" );
		Assert.isTrue( parts.length == 2, "FolderDescriptor URI must contain 2 segments separated with :" );
		Assert.isTrue( parts[1].endsWith( "/" ), "FolderDescriptor URI must end with trailing /" );

		return of( parts[0], parts[1] );
	}

	/**
	 * Create a descriptor for the folder in the specified repository.
	 *
	 * @param repositoryId in which the folder exists
	 * @param folderId     in the repository
	 * @return descriptor
	 */
	public static FolderDescriptor of( @NonNull String repositoryId, String folderId ) {
		Assert.isTrue( !StringUtils.contains( repositoryId, ":" ), "repositoryId should not contain :" );

		String id = removeStart( removeEnd( defaultIfEmpty( replace( folderId, "\\", "/" ), "/" ), "/" ), "/" );

		if ( !StringUtils.isEmpty( id ) ) {
			Matcher matcher = INVALID_FOLDER_ID.matcher( id );
			if ( matcher.find() ) {
				throw new IllegalArgumentException( "folderId should not contain colon, dot-only folder names or be only whitespace" );
			}
		}
		else {
			id = null;
		}

		return new FolderDescriptor( repositoryId, id );
	}
}
