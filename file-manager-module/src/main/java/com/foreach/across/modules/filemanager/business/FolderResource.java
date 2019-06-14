package com.foreach.across.modules.filemanager.business;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
	 * @return name of this folder
	 */
	default String getFolderName() {
		String folderId = getDescriptor().getFolderId();
		int lastSeparator = StringUtils.lastIndexOf( folderId, "/" );
		return lastSeparator > 0 ? folderId.substring( lastSeparator + 1 ) : StringUtils.defaultString( folderId );
	}

	/**
	 * @return parent folder resource
	 */
	Optional<FolderResource> getParentFolderResource();

	/**
	 * Get the folder resource identified by the relative path.
	 * If the target is not a valid folder resource, an exception should be thrown.
	 *
	 * @param relativePath to the folder
	 * @return folder resource
	 */
	default FolderResource getFolderResource( @NonNull String relativePath ) {
		return Optional.ofNullable( getResource( relativePath.endsWith( "/" ) ? relativePath : relativePath + "/" ) )
		               .filter( FolderResource.class::isInstance )
		               .map( FolderResource.class::cast )
		               .orElseThrow( () -> new IllegalArgumentException( "Relative path '" + relativePath + "' is not a valid folder resource" ) );
	}

	/**
	 * Get the file resource identified by the relative path.
	 * If the target is not a valid file resource, an exception should be thrown.
	 *
	 * @param relativePath to the file
	 * @return file resource
	 */
	default FileResource getFileResource( @NonNull String relativePath ) {
		return Optional.ofNullable( getResource( relativePath ) )
		               .filter( FileResource.class::isInstance )
		               .map( FileResource.class::cast )
		               .orElseThrow( () -> new IllegalArgumentException( "Relative path '" + relativePath + "' is not a valid file resource" ) );
	}

	/**
	 * Get the resource identified by the relative path.
	 * If the target is not a valid resource, an exception should be thrown.
	 * <p/>
	 * A path ending with / indicates a folder resource, otherwise a file resource should be returned.
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
	default FileResource createFileResource() {
		return getFileResource( UUID.randomUUID().toString().replace( "-", "" ) );
	}

	/**
	 * List the file resources that are direct children of this folder.
	 * Should return an empty collection if the folder does not exist.
	 *
	 * @return collection of file resources
	 */
	default Collection<FileResource> listFiles() {
		return listResources( false, FileResource.class );
	}

	/**
	 * List the folder resources that are direct children of this folder.
	 * Should return an empty collection if the folder does not exist.
	 *
	 * @return collection of folder resources
	 */
	default Collection<FolderResource> listFolders() {
		return listResources( false, FolderResource.class );

	}

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
	default <U extends FileRepositoryResource> Collection<U> listResources( boolean recurseFolders, Class<U> resourceType ) {
		return findResources( recurseFolders ? "/**" : "/*", resourceType );
	}

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
	default Collection<FileRepositoryResource> listResources( boolean recurseFolders ) {
		return findResources( recurseFolders ? "/**" : "/*" );
	}

	/**
	 * Find all file resources matching the given ANT pattern.
	 * Equivalent of {@link #findResources(String, Class)} with a {@link FileResource} type filter.
	 *
	 * @param pattern to match
	 * @return resources
	 */
	default Collection<FileResource> findFiles( @NonNull String pattern ) {
		return findResources( pattern, FileResource.class );
	}

	/**
	 * Find all resources matching the given ANT pattern.
	 *
	 * @param pattern      to match
	 * @param resourceType type of resources to return
	 * @return resources
	 */
	default <U extends FileRepositoryResource> Collection<U> findResources( @NonNull String pattern, Class<U> resourceType ) {
		return findResources( pattern )
				.stream()
				.filter( resourceType::isInstance )
				.map( resourceType::cast )
				.collect( Collectors.toList() );
	}

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
	 * also not throw an exception but simply return {@code false}. Because of this,
	 * this method can be used to avoid a call to {@link #exists()} if it would be
	 * immediately followed by create anyway.
	 *
	 * @return true if folder has been created - {@link #exists()} should return {@code true}
	 */
	boolean create();

	/**
	 * @return true if the folder does not have any children
	 */
	default boolean isEmpty() {
		return listResources( false ).isEmpty();
	}
}
