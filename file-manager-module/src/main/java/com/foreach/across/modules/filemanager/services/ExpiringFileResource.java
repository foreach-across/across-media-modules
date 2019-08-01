package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileResource;

/**
 * Extends the default file resource interface with tracking properties which can be used for expiration.
 *
 * @author Arne Vandamme
 * @see ExpiringFileRepository
 * @see CachingFileRepository
 * @since 1.4.0
 */
public interface ExpiringFileResource extends FileResource
{
	/**
	 * @return timestamp when the the actual content of the file resource was last accessed.
	 */
	long getLastAccessTime();

	/**
	 * @return timestamp when this file resource was created (what that means depends on the actual implementation)
	 */
	long getCreationTime();
}
