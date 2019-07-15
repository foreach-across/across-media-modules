package com.foreach.across.modules.filemanager.business;

import java.net.URI;

/**
 * Represent a single resource in a {@link com.foreach.across.modules.filemanager.services.FileRepository}.
 * Either a {@link FileResource} or a {@link FolderResource}.
 *
 * @author Arne Vandamme
 * @see FileResource
 * @see FolderResource
 * @since 1.4.0
 */
public interface FileRepositoryResource
{
	/**
	 * @return the unique descriptor to this resource
	 */
	FileRepositoryResourceDescriptor getDescriptor();

	/**
	 * @return resource URI to this resource
	 */
	default URI getURI() {
		return getDescriptor().toResourceURI();
	}

	/**
	 * @return true if the resource exists
	 */
	boolean exists();
}
