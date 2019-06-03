package com.foreach.across.modules.filemanager.business;

import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents a single {@link com.foreach.across.modules.filemanager.services.FileRepository} folder,
 * identified by a {@link FolderDescriptor}.
 * <p/>
 * This interface has several default methods that implementors can override for performance reasons,
 * usually related to file/folder differences and searching resources.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
public interface FolderResource extends FileRepositoryResource
{
	@Override
	FolderDescriptor getDescriptor();

	/**
	 * @return parent folder resource
	 */
	Optional<FolderResource> getParentFolderResource();

	/**
	 * Get the folder resource identified by the relative path.
	 * If the target is not a valid folder resource, an exception will be thrown.
	 *
	 * @param relativePath to the folder
	 * @return folder resource
	 */
	FolderResource getFolderResource( String relativePath );

	/**
	 * Get the file resource identified by the relative path.
	 * If the target is not a valid file resource, an exception will be thrown.
	 *
	 * @param relativePath to the file
	 * @return file resource
	 */
	FileResource getFileResource( String relativePath );

	/**
	 * Get the resource identified by the relative path.
	 * If the target is not a valid resource, an exception will be thrown.
	 *
	 * @param relativePath to the resource
	 * @return resource
	 */
	FileRepositoryResource getResource( String relativePath );

	/**
	 * Create a new unique (writable) file resource in this folder.
	 *
	 * @return file resource
	 */
	FileResource createFileResource();

	/**
	 * List the file resources that are direct children of this folder.
	 * Should return an empty collection if the folder does not exist.
	 *
	 * @return collection of file resources
	 */
	Collection<FileResource> listFiles();

	/**
	 * List the folder resources that are direct children of this folder.
	 * Should return an empty collection if the folder does not exist.
	 *
	 * @return collection of folder resources
	 */
	Collection<FolderResource> listFolders();

	/**
	 * List resources that are children of this folder.
	 * Depending on the parameter only direct children will be returned ({@code false})
	 * or the entire sub-tree will be returned.
	 * <p/>
	 * Should return an empty collection if the folder does not exist.
	 *
	 * @param recurseFolders true if child folders should be navigated as well
	 * @param resourceType   type of resources that should be returned
	 * @return collection of resources
	 */
	<U extends FileRepositoryResource> Collection<U> listChildren( boolean recurseFolders, Class<U> resourceType );

	/**
	 * List resources that are children of this folder.
	 * Depending on the parameter only direct children will be returned ({@code false})
	 * or the entire sub-tree will be returned.
	 * <p/>
	 * Should return an empty collection if the folder does not exist.
	 *
	 * @param recurseFolders true if child folders should be navigated as well
	 * @return collection of resources
	 */
	Collection<FileRepositoryResource> listChildren( boolean recurseFolders );

	/**
	 * Find all resources matching the given ANT pattern.
	 *
	 * @param pattern      to match
	 * @param resourceType type of resources to return
	 * @return resources
	 */
	<U extends FileRepositoryResource> Collection<U> findResources( @NonNull String pattern, Class<U> resourceType );

	/**
	 * Find all resources matching the given ANT pattern.
	 *
	 * @param pattern to match
	 * @return resources
	 */
	Collection<FileRepositoryResource> findResources( @NonNull String pattern );

	/**
	 * Delete this folder. The parameter value indicates if non-empty folders should be
	 * deleted after first deleting all children ({@code true}) or if the folder should
	 * not be deleted if there still are children ({@code false}).
	 * <p/>
	 * The return value only confirms that delete has failed but does not necessarily
	 * ensure that the folder has been deleted. A value of {@code true} only indicates that
	 * the delete command has been attempted, whereas {@code false} implies it has not
	 * (because the folder is not empty for example).
	 *
	 * @param deleteChildren true if existing children should also be deleted
	 * @return false if delete has failed
	 */
	boolean delete( boolean deleteChildren );

	/**
	 * Delete all children of this folder, but not the folder itself.
	 *
	 * @return true if an attempt has been made to delete all children
	 */
	boolean deleteChildren();

	/**
	 * Create the folder itself, without adding any files to it. Usually only useful if
	 * {@link #exists()} returns {@code false}.
	 * <p/>
	 * Not every file system might support empty folder creation, in which case the return
	 * value is expected to be {@code false}. Creating a folder that already exists should
	 * also not throw an exception but simply return {@code false}.
	 *
	 * @return true if folder has been created - {@link #exists()} should return {@code true}
	 */
	boolean create();

	/**
	 * @return true if the folder does not have any children
	 */
	boolean isEmpty();
}
