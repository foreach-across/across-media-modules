package com.foreach.across.modules.filemanager.utils;

import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.web.FileReferenceController;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class FileReferenceUtils
{
	public static String getDownloadUrl( FileReference fileReference ) {
		return FileReferenceController.BASE_PATH + "/" + fileReference.getUuid();
	}
}
