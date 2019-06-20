package com.foreach.across.modules.filemanager.services;

/**
 * Interface that can be implemented by a {@link FileRepository}.
 * The {@link #setFileManager(FileManager)} will be called when the repository is attached to the {@link FileManager}.
 * The {@link #shutdown()} method will then be called when the file manager is being destroyed.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
public interface FileManagerAware
{
	/**
	 * Set the {@link FileManager} to which the repository is being attached.
	 *
	 * @param fileManager instance
	 */
	void setFileManager( FileManager fileManager );

	/**
	 * Called when the file manager is being destroyed.
	 */
	default void shutdown() {
	}
}
