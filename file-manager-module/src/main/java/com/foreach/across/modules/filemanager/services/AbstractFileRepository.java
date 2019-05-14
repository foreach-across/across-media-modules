package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;

/**
 * Base class for {@link FileRepository} implementations that delegate all resource
 * methods to the {@link FileResource} instance. Implementations should usually only
 * implement {@link #buildFileResource(FileDescriptor)} and possibly extend {@link #validateFileDescriptor(FileDescriptor)}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
public abstract class AbstractFileRepository implements FileRepository
{
	/**
	 * Validates if the descriptor is valid for this file repository.
	 * A valid descriptor means it should be possible to have an actual file resource
	 * matching it, it does not mean that the resource should already exist.
	 *
	 * @param descriptor to the file resource
	 */
	protected void validateFileDescriptor( FileDescriptor descriptor ) {

	}

	/**
	 * Create the {@link FileResource} for a file descriptor.
	 * Basic validation of the descriptor will have been done in {@link #validateFileDescriptor(FileDescriptor)},
	 * this method should return the actual file resource that can be used.
	 *
	 * @param descriptor to the file resource
	 * @return file resource to use
	 */
	abstract FileResource buildFileResource( FileDescriptor descriptor );
}
