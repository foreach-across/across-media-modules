package com.foreach.across.modules.filemanager.business;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
public interface FolderResource
{
	FolderDescriptor getFolderDescriptor();

	Optional<FolderResource> getParentFolder();

	boolean exists();

	FolderResource createFolderResource( String relativePath );

	FolderResource getFolderResource( String relativePath );

	FileResource createFileResource();

	FileResource getFileResource( String relativePath );

	Collection<FileResource> listFiles( boolean recurseFolders );

	Collection<FolderResource> listFolders( boolean recurseFolders );

	//Collection<FileRepositoryResource> listChildren( boolean recurseFolders );

	boolean delete( boolean deleteChildren );

	boolean deleteChildren();

	boolean create();

	boolean isEmpty();
}
