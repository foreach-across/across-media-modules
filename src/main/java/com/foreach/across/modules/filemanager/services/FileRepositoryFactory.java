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
