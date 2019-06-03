package com.foreach.across.modules.filemanager.business;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.net.URI;

/**
 * Represents a descriptor (unique identifier) to a {@link FileRepositoryResource}.
 *
 * @author Arne Vandamme
 * @see FileDescriptor
 * @see FolderDescriptor
 * @since 1.4.0
 */
public interface FileRepositoryResourceDescriptor extends Serializable
{
	/**
	 * @return actual resource URI
	 */
	@SneakyThrows
	default URI toResourceURI() {
		return new URI( "axfs://" + getUri() );
	}

	/**
	 * @return internal URI string representation of the descriptor
	 */
	String getUri();

	/**
	 * @return Unique id of the repository this resource belongs to.
	 */
	String getRepositoryId();
}
