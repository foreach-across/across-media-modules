package com.foreach.across.modules.filemanager.utils;

import com.foreach.across.modules.filemanager.business.reference.FileReference;

/**
 * Provides shortcut utility methods for working with {@link FileReference}s.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
public class FileReferenceUtils
{
	/**
	 * Provides the download url for a given {@link FileReference}.
	 * The download url should be converted using a {@link com.foreach.across.modules.web.context.WebAppLinkBuilder}.
	 *
	 * @param fileReference that references the file to download
	 * @return a prefixed url.
	 * @see com.foreach.across.modules.filemanager.config.UrlPrefixingConfiguration
	 */
	public static String getDownloadUrl( FileReference fileReference ) {
		return "@fileReference:/" + fileReference.getUuid();
	}
}
