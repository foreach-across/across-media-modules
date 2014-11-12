package com.foreach.across.modules.filemanager.services;

/**
 * PathGenerator interface can be used to dynamically generate a path
 * to distribute files in a repository.
 *
 * @see com.foreach.across.modules.filemanager.services.LocalFileRepository
 * @see com.foreach.across.modules.filemanager.services.DateFormatPathGenerator
 */
public interface PathGenerator
{
	/**
	 * Generate a path for a new file.
	 *
	 * @return Path string or null if no sub folder should be used.
	 */
	String generatePath();
}
