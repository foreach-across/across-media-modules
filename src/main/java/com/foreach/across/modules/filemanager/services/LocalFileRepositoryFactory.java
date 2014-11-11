package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileStorageException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Standard implementation that creates LocalFileRepository instances.
 * This factory takes a root directory, every created instance will get its own root folder
 * that is a direct child of the factories root folder.  The name of the child will be the same
 * as the repository id.
 *
 * @see com.foreach.across.modules.filemanager.services.LocalFileRepository
 */
public class LocalFileRepositoryFactory implements FileRepositoryFactory
{
	private static final Logger LOG = LoggerFactory.getLogger( LocalFileRepositoryFactory.class );

	private final String rootFolder;
	private final PathGenerator pathGenerator;

	public LocalFileRepositoryFactory( String rootFolder,
	                                   PathGenerator pathGenerator ) {
		this.rootFolder = rootFolder;
		this.pathGenerator = pathGenerator;
	}

	/**
	 * Attempt to create a new FileRepository instance with the given repository id.
	 * If creation is not possible, null should be returned.
	 *
	 * @param repositoryId Unique id of the FileRepository.
	 * @return FileRepository instance or null.
	 */
	@Override
	public FileRepository create( String repositoryId ) {
		File directory = Paths.get( rootFolder, repositoryId ).toFile();

		LOG.info( "Creating a new LocalFileRepository with root folder {}", directory );

		if ( !directory.exists() ) {
			try {
				FileUtils.forceMkdir( directory );
			}
			catch ( IOException ioe ) {
				throw new FileStorageException( ioe );
			}
		}

		LocalFileRepository repository = new LocalFileRepository( repositoryId, directory.getAbsolutePath() );
		repository.setPathGenerator( pathGenerator );

		return repository;
	}
}
